package com.example.viagourmet.data.repository

import com.example.viagourmet.Presentacion.session.RolUsuario
import com.example.viagourmet.Presentacion.session.UsuarioSesion
import com.example.viagourmet.data.api.CafeteriaApiService
import com.example.viagourmet.data.dao.UsuarioDao
import com.example.viagourmet.data.Local.mapper.crearUsuarioEntity
import com.example.viagourmet.data.Local.mapper.toSesion
import com.example.viagourmet.data.Local.util.hashPassword
import com.example.viagourmet.data.model.request.LoginRequest
import com.example.viagourmet.data.model.request.RegistroClienteRequest
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthResult {
    data class Success(val usuario: UsuarioSesion) : AuthResult()
    data class Error(val mensaje: String) : AuthResult()
}

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

        // ── Login de EMPLEADO: autenticar contra la API (BCrypt en el servidor) ──
        return try {
            val resp = api.loginEmpleado(
                LoginRequest.LoginRequest(
                    email = emailNorm,
                    password = password
                )
            )
            if (resp.isSuccessful && resp.body()?.data != null) {
                val empleadoData = resp.body()!!.data!!
                val rolUsuario = when (empleadoData.rol.lowercase()) {
                    "admin"    -> RolUsuario.ADMIN
                    "cocinero" -> RolUsuario.COCINERO
                    "cajero",
                    "mesero"   -> RolUsuario.EMPLEADO
                    else       -> RolUsuario.EMPLEADO
                }
                // Guardar también en Room local para acceso offline
                try {
                    val entity = crearUsuarioEntity(
                        nombre    = empleadoData.nombre,
                        apellido  = empleadoData.apellido,
                        telefono  = null,
                        email     = emailNorm,
                        password  = password,
                        rol       = rolUsuario,
                        ahora     = nowString()
                    )
                    val entityConId = entity.copy(id = empleadoData.id)
                    dao.insertUsuario(entityConId)
                } catch (_: Exception) { /* ya existe en Room, ignorar */ }

                AuthResult.Success(
                    UsuarioSesion(
                        id     = empleadoData.id,
                        nombre = empleadoData.nombre,
                        email  = emailNorm,
                        rol    = rolUsuario
                    )
                )
            } else {
                // La API rechazó las credenciales → intentar como cliente
                loginCliente(emailNorm, password)
            }
        } catch (e: Exception) {
            // Sin conexión → intentar Room local
            loginLocal(emailNorm, password)
        }
    }

    /**
     * Login de CLIENTE contra la API.
     * La API usa BCrypt; la app envía la contraseña en texto plano al endpoint /login.
     */
    private suspend fun loginCliente(email: String, password: String): AuthResult {
        return try {
            val resp = api.loginCliente(
                com.example.viagourmet.data.model.request.LoginClienteRequest.LoginClienteRequest(
                    email = email,
                    contrasena = password
                )
            )
            if (resp.isSuccessful && resp.body()?.data != null) {
                val clienteData = resp.body()!!.data!!
                // Guardar en Room para offline
                try {
                    val entity = crearUsuarioEntity(
                        nombre    = clienteData.nombre,
                        apellido  = clienteData.apellido,
                        telefono  = clienteData.telefono,
                        email     = email,
                        password  = password,
                        rol       = RolUsuario.CLIENTE,
                        ahora     = nowString()
                    )
                    dao.insertUsuario(entity.copy(id = clienteData.id))
                } catch (_: Exception) { /* ya existe */ }

                AuthResult.Success(
                    UsuarioSesion(
                        id     = clienteData.id,
                        nombre = clienteData.nombre,
                        email  = email,
                        rol    = RolUsuario.CLIENTE
                    )
                )
            } else {
                AuthResult.Error("Email o contraseña incorrectos")
            }
        } catch (e: Exception) {
            loginLocal(email, password)
        }
    }

    /**
     * Fallback offline: buscar en Room con hash SHA-256 local.
     */
    private suspend fun loginLocal(email: String, password: String): AuthResult {
        val hash = hashPassword(password)
        val usuarioLocal = dao.login(email, hash)
        return if (usuarioLocal != null) {
            AuthResult.Success(usuarioLocal.toSesion())
        } else {
            AuthResult.Error("Sin conexión. Verifica tu red e intenta de nuevo.")
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
        if (nombre.isBlank()) return AuthResult.Error("El nombre es obligatorio")
        if (email.isBlank() || !email.contains("@")) return AuthResult.Error("Email inválido")
        if (password.length < 6) return AuthResult.Error("La contraseña debe tener al menos 6 caracteres")
        if (password != confirmarPassword) return AuthResult.Error("Las contraseñas no coinciden")

        val emailNorm = email.trim().lowercase()

        if (rol != RolUsuario.CLIENTE) {
            return AuthResult.Error(
                "Los empleados son creados por el administrador del sistema."
            )
        }

        // ── Registro de CLIENTE ───────────────────────────────────────────────
        return try {
            // Verificar si ya existe en la API
            val existeResp = api.buscarClientePorEmail(emailNorm)
            if (existeResp.isSuccessful && existeResp.body()?.data != null) {
                return AuthResult.Error("Ya existe una cuenta con ese email")
            }

            // Crear en la API — AHORA SÍ incluye la contraseña en texto plano
            // La API aplica BCrypt antes de guardar en la BD
            val crearResp = api.crearCliente(
                RegistroClienteRequest.RegistroClienteRequest(
                    nombre      = nombre.trim(),
                    apellido    = apellido?.trim()?.ifBlank { null },
                    telefono    = telefono?.trim()?.ifBlank { null },
                    email       = emailNorm,
                    contrasena  = password  // ← CAMPO AGREGADO
                )
            )
            if (!crearResp.isSuccessful || crearResp.body()?.data == null) {
                return AuthResult.Error("Error al crear la cuenta. Intenta de nuevo.")
            }
            val clienteDto = crearResp.body()!!.data!!

            // Guardar contraseña (hash) en Room local para login offline
            val entity = crearUsuarioEntity(
                nombre    = nombre.trim(),
                apellido  = apellido?.trim()?.ifBlank { null },
                telefono  = telefono?.trim()?.ifBlank { null },
                email     = emailNorm,
                password  = password,
                rol       = RolUsuario.CLIENTE,
                ahora     = nowString(),
                fotoCredencialUri = fotoCredencialUri
            )
            try {
                dao.insertUsuario(entity.copy(id = clienteDto.id))
            } catch (_: Exception) { /* ya existe */ }

            AuthResult.Success(
                UsuarioSesion(
                    id     = clienteDto.id,
                    nombre = nombre.trim(),
                    email  = emailNorm,
                    rol    = RolUsuario.CLIENTE
                )
            )
        } catch (e: Exception) {
            // Sin red → registrar solo local
            if (dao.findByEmail(emailNorm) != null) {
                return AuthResult.Error("Ya existe una cuenta local con ese email")
            }
            try {
                val entity = crearUsuarioEntity(
                    nombre    = nombre.trim(),
                    apellido  = apellido?.trim()?.ifBlank { null },
                    telefono  = telefono?.trim()?.ifBlank { null },
                    email     = emailNorm,
                    password  = password,
                    rol       = RolUsuario.CLIENTE,
                    ahora     = nowString(),
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

    private fun nowString(): String {
        val c = Calendar.getInstance()
        return "%04d-%02d-%02dT%02d:%02d:%02d".format(
            c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
            c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND)
        )
    }
}