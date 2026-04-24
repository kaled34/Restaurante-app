package com.example.viagourmet.data.repository

import android.content.Context
import android.util.Log
import com.example.viagourmet.Presentacion.session.RolUsuario
import com.example.viagourmet.Presentacion.session.UsuarioSesion
import com.example.viagourmet.Presentacion.session.SessionManager
import com.example.viagourmet.data.Local.mapper.crearUsuarioEntity
import com.example.viagourmet.data.Local.mapper.toSesion
import com.example.viagourmet.data.Local.util.hashPassword
import com.example.viagourmet.data.api.CafeteriaApiService
import com.example.viagourmet.data.dao.UsuarioDao
import com.example.viagourmet.data.model.request.LoginRequest
import com.example.viagourmet.data.model.request.RegistroClienteRequest
import com.example.viagourmet.data.model.request.RegistroEmpleadoRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
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
    private val api: CafeteriaApiService,
    private val sessionManager: com.example.viagourmet.Presentacion.session.SessionManager,
    @ApplicationContext private val context: Context
) {

    suspend fun login(email: String, password: String): AuthResult {
        if (email.isBlank() || password.isBlank())
            return AuthResult.Error("Email y contraseña son obligatorios")

        val emailNorm = email.trim().lowercase()

        return try {
            val respEmpleado = api.loginEmpleado(
                LoginRequest.LoginRequest(email = emailNorm, password = password)
            )
            if (respEmpleado.isSuccessful && respEmpleado.body()?.data != null) {
                val empleadoData = respEmpleado.body()!!.data!!
                val rolUsuario = when (empleadoData.rol.lowercase()) {
                    "admin"          -> RolUsuario.ADMIN
                    "cocinero"       -> RolUsuario.COCINERO
                    else             -> RolUsuario.EMPLEADO
                }
                
                // Intentar recuperar foto local si ya existía este usuario
                val localUser = dao.findByEmail(emailNorm)
                val fotoUri = localUser?.fotoCredencialUri

                val sesion = UsuarioSesion(
                    id     = empleadoData.id,
                    nombre = empleadoData.nombre,
                    apellido = empleadoData.apellido,
                    email  = emailNorm,
                    rol    = rolUsuario,
                    fotoUri = fotoUri
                )
                
                // Guardar sesión y opcionalmente vincular token si ya lo tenemos
                sessionManager.guardarSesion(sesion)
                
                AuthResult.Success(sesion)
            } else {
                loginCliente(emailNorm, password)
            }
        } catch (e: Exception) {
            loginLocal(emailNorm, password)
        }
    }

    private suspend fun loginCliente(email: String, password: String): AuthResult {
        return try {
            val respCliente = api.loginCliente(
                com.example.viagourmet.data.model.request.LoginRequest.LoginRequest(
                    email    = email,
                    password = password
                )
            )
            if (respCliente.isSuccessful && respCliente.body()?.data != null) {
                val clienteData = respCliente.body()!!.data!!
                val localUser = dao.findByEmail(email)
                
                val sesion = UsuarioSesion(
                    id     = clienteData.id,
                    nombre = clienteData.nombre,
                    apellido = clienteData.apellido,
                    telefono = clienteData.telefono,
                    email  = email,
                    rol    = RolUsuario.CLIENTE,
                    fotoUri = localUser?.fotoCredencialUri
                )
                
                sessionManager.guardarSesion(sesion)
                
                AuthResult.Success(sesion)
            } else {
                loginLocal(email, password)
            }
        } catch (e: Exception) {
            loginLocal(email, password)
        }
    }

    suspend fun vincularTokenFcm(token: String) {
        val sesion = sessionManager.obtenerSesion() ?: return
        if (sesion.rol == RolUsuario.CLIENTE) {
            try {
                api.actualizarFcmToken(
                    clienteId = sesion.id,
                    request = com.example.viagourmet.data.model.request.FcmTokenRequest(token = token)
                )
                Log.d("AuthRepository", "Token FCM vinculado para cliente ${sesion.id}")
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error vinculando token FCM: ${e.message}")
            }
        }
        // Si en el futuro hay endpoint para empleados, se añadiría aquí
    }

    private suspend fun loginLocal(email: String, password: String): AuthResult {
        val hash = hashPassword(password)
        val local = dao.login(email, hash)
        return if (local != null) {
            AuthResult.Success(local.toSesion())
        } else {
            AuthResult.Error("Email o contraseña incorrectos")
        }
    }

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
        if (password != confirmarPassword) return AuthResult.Error("Las contraseñas no coinciden")

        val emailNorm = email.trim().lowercase()

        return try {
            val resp = if (rol == RolUsuario.CLIENTE) {
                api.crearCliente(RegistroClienteRequest.RegistroClienteRequest(
                    nombre = nombre.trim(), apellido = apellido, telefono = telefono, email = emailNorm, contrasena = password
                ))
            } else {
                val rolParaServer = when(rol) {
                    RolUsuario.ADMIN    -> "admin"
                    RolUsuario.COCINERO -> "cocinero"
                    else                -> "mesero"
                }
                api.crearEmpleado(RegistroEmpleadoRequest(
                    nombre = nombre.trim(), apellido = apellido, email = emailNorm, contrasena = password, rol = rolParaServer
                ))
            }

            if (resp.isSuccessful && resp.body()?.data != null) {
                val data = resp.body()!!.data!!
                val id = if (data is com.example.viagourmet.data.model.ClienteDto) data.id else (data as com.example.viagourmet.data.model.EmpleadoDto).id
                
                // PERSISTENCIA DE FOTO: Guardamos la URI en Room
                val entity = crearUsuarioEntity(
                    nombre = nombre, apellido = apellido, telefono = telefono,
                    email = emailNorm, password = password, rol = rol,
                    ahora = nowString(), fotoCredencialUri = fotoCredencialUri
                )
                dao.insertUsuario(entity.copy(id = id))

                AuthResult.Success(
                    UsuarioSesion(id = id, nombre = nombre, apellido = apellido, telefono = telefono, email = emailNorm, rol = rol, fotoUri = fotoCredencialUri)
                )
            } else {
                registrarLocal(nombre, apellido, telefono, emailNorm, password, rol, fotoCredencialUri)
            }
        } catch (e: Exception) {
            registrarLocal(nombre, apellido, telefono, emailNorm, password, rol, fotoCredencialUri)
        }
    }

    private suspend fun registrarLocal(n: String, a: String?, t: String?, e: String, p: String, r: RolUsuario, f: String?): AuthResult {
        val entity = crearUsuarioEntity(n, a, t, e, p, r, nowString(), f)
        val id = dao.insertUsuario(entity).toInt()
        return AuthResult.Success(UsuarioSesion(id, n, a, t, e, r, f))
    }

    private fun nowString(): String {
        val c = Calendar.getInstance()
        return "%04d-%02d-%02dT%02d:%02d:%02d".format(
            c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
            c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND)
        )
    }
}
