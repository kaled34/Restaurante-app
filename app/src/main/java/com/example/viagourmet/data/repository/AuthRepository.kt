package com.example.viagourmet.data.repository

import com.example.viagourmet.Presentacion.session.RolUsuario
import com.example.viagourmet.Presentacion.session.UsuarioSesion
import com.example.viagourmet.data.api.CafeteriaApiService
import com.example.viagourmet.data.dao.UsuarioDao
import com.example.viagourmet.data.local.mapper.crearUsuarioEntity
import com.example.viagourmet.data.local.mapper.toSesion
import com.example.viagourmet.data.local.util.hashPassword
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val usuario: UsuarioSesion) : AuthResult()
    data class Error(val mensaje: String) : AuthResult()
}

/**
 * AuthRepository unificado:
 *
 * - CLIENTES: se crean/buscan en la API remota (tabla `clientes` del servidor).
 *   El id que devuelve la API se guarda en sesión para asociar pedidos.
 *
 * - EMPLEADOS: se autentican contra Room local (tabla `usuarios`).
 *   Los empleados no se registran desde la app; el admin los crea
 *   directamente en la BD o en la API (endpoint /api/v1/empleados POST).
 *
 * Flujo login cliente:
 *   1. Buscar cliente por email en la API → si existe, verificar contraseña
 *      (la contraseña del cliente se guarda en Room solo como hash local
 *       para verificación offline — la API no tiene campo contraseña en clientes).
 *   2. Si no existe en la API, error "cuenta no encontrada".
 *
 * Flujo login empleado:
 *   1. Buscar en Room por email + hash. Si existe y tiene rol de empleado, OK.
 *
 * Flujo registro cliente:
 *   1. Crear cliente en la API → obtener id real.
 *   2. Guardar contraseña (hash) en Room local para poder hacer login offline.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val dao: UsuarioDao,
    private val api: CafeteriaApiService
) {

    // ── Login ─────────────────────────────────────────────────────────────────

    suspend fun login(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank())
            return AuthResult.Error("Email y contraseña son obligatorios")

        val emailNorm = email.trim().lowercase()
        val hash = hashPassword(password)

        // 1. Intentar como empleado (Room local)
        val usuarioLocal = dao.login(emailNorm, hash)
        if (usuarioLocal != null) {
            val sesion = usuarioLocal.toSesion()
            // Solo dejar pasar si el rol es de empleado/admin/cocinero
            if (sesion.rol != RolUsuario.CLIENTE) {
                return AuthResult.Success(sesion)
            }
        }

        // 2. Intentar como cliente (API remota)
        return try {
            val resp = api.buscarClientePorEmail(emailNorm)
            if (resp.isSuccessful && resp.body()?.data != null) {
                val clienteDto = resp.body()!!.data!!
                // Verificar hash local (guardado al registrar)
                val localUser = dao.login(emailNorm, hash)
                    ?: return AuthResult.Error("Contraseña incorrecta")
                AuthResult.Success(
                    UsuarioSesion(
                        id = clienteDto.id,          // id real del servidor
                        nombre = clienteDto.nombre,
                        email = emailNorm,
                        rol = RolUsuario.CLIENTE
                    )
                )
            } else {
                AuthResult.Error("No existe una cuenta con ese email")
            }
        } catch (e: Exception) {
            // Sin red → intentar con Room local si es cliente
            val local = dao.login(emailNorm, hash)
            if (local != null && local.toSesion().rol == RolUsuario.CLIENTE) {
                AuthResult.Success(local.toSesion())
            } else {
                AuthResult.Error("Sin conexión y no hay cuenta local: ${e.message}")
            }
        }
    }

    // ── Registro de clientes ──────────────────────────────────────────────────

    suspend fun registrar(
        nombre: String,
        apellido: String?,
        telefono: String?,
        email: String,
        password: String,
        confirmarPassword: String,
        rol: RolUsuario,
        fotoCredencialUri: String? = null
    ): AuthResult {
        // Validaciones básicas
        if (nombre.isBlank()) return AuthResult.Error("El nombre es obligatorio")
        if (email.isBlank() || !email.contains("@")) return AuthResult.Error("Email inválido")
        if (password.length < 6) return AuthResult.Error("La contraseña debe tener al menos 6 caracteres")
        if (password != confirmarPassword) return AuthResult.Error("Las contraseñas no coinciden")

        val emailNorm = email.trim().lowercase()

        // Empleados y cocineros no se pueden registrar desde la app
        if (rol != RolUsuario.CLIENTE) {
            return AuthResult.Error(
                "Los empleados son creados por el administrador del sistema. " +
                        "Contacta al administrador para obtener tu acceso."
            )
        }

        // ── Registro de CLIENTE en la API ──────────────────────────────────
        return try {
            // Verificar si ya existe en la API
            val existeResp = api.buscarClientePorEmail(emailNorm)
            if (existeResp.isSuccessful && existeResp.body()?.data != null) {
                return AuthResult.Error("Ya existe una cuenta con ese email")
            }

            // Crear en la API
            val crearResp = api.crearCliente(
                com.example.viagourmet.data.model.request.RegistroClienteRequest.RegistroClienteRequest(
                    nombre = nombre.trim(),
                    apellido = apellido?.trim()?.ifBlank { null },
                    telefono = telefono?.trim()?.ifBlank { null },
                    email = emailNorm
                )
            )
            if (!crearResp.isSuccessful || crearResp.body()?.data == null) {
                return AuthResult.Error("Error al crear la cuenta en el servidor")
            }
            val clienteDto = crearResp.body()!!.data!!

            // Guardar hash local en Room para login offline
            val entity = crearUsuarioEntity(
                nombre = nombre.trim(),
                apellido = apellido?.trim()?.ifBlank { null },
                telefono = telefono?.trim()?.ifBlank { null },
                email = emailNorm,
                password = password,
                rol = RolUsuario.CLIENTE,
                ahora = nowString(),
                fotoCredencialUri = fotoCredencialUri
            )
            // Forzar el id del servidor para poder identificar al cliente
            val entityConId = entity.copy(id = clienteDto.id)
            try { dao.insertUsuario(entityConId) } catch (_: Exception) { /* ya existe */ }

            AuthResult.Success(
                UsuarioSesion(
                    id = clienteDto.id,
                    nombre = nombre.trim(),
                    email = emailNorm,
                    rol = RolUsuario.CLIENTE
                )
            )
        } catch (e: Exception) {
            // Sin red → registrar solo local
            if (dao.findByEmail(emailNorm) != null) {
                return AuthResult.Error("Ya existe una cuenta local con ese email")
            }
            try {
                val entity = crearUsuarioEntity(
                    nombre = nombre.trim(),
                    apellido = apellido?.trim()?.ifBlank { null },
                    telefono = telefono?.trim()?.ifBlank { null },
                    email = emailNorm,
                    password = password,
                    rol = RolUsuario.CLIENTE,
                    ahora = nowString(),
                    fotoCredencialUri = fotoCredencialUri
                )
                val id = dao.insertUsuario(entity).toInt()
                AuthResult.Success(
                    UsuarioSesion(id = id, nombre = nombre.trim(), email = emailNorm, rol = RolUsuario.CLIENTE)
                )
            } catch (ex: android.database.sqlite.SQLiteConstraintException) {
                AuthResult.Error("Ya existe una cuenta con ese email")
            } catch (ex: Exception) {
                AuthResult.Error("Error al crear la cuenta: ${ex.message}")
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun nowString(): String {
        val c = Calendar.getInstance()
        return "%04d-%02d-%02dT%02d:%02d:%02d".format(
            c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
            c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND)
        )
    }
}