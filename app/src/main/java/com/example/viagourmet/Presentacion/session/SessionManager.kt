package com.example.viagourmet.Presentacion.session

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class RolUsuario { CLIENTE, EMPLEADO, COCINERO, ADMIN }

data class UsuarioSesion(
    val id: Int,
    val nombre: String,
    val email: String,
    val rol: RolUsuario
)

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("vg_session", Context.MODE_PRIVATE)

    private var usuarioActual: UsuarioSesion? = null

    fun guardarSesion(usuario: UsuarioSesion) {
        usuarioActual = usuario
        prefs.edit()
            .putInt("user_id", usuario.id)
            .putString("user_nombre", usuario.nombre)
            .putString("user_email", usuario.email)
            .putString("user_rol", usuario.rol.name)
            .apply()
    }

    fun obtenerSesion(): UsuarioSesion? {
        if (usuarioActual != null) return usuarioActual
        val id = prefs.getInt("user_id", -1)
        if (id == -1) return null
        val nombre = prefs.getString("user_nombre", "") ?: ""
        val email = prefs.getString("user_email", "") ?: ""
        val rolStr = prefs.getString("user_rol", "CLIENTE") ?: "CLIENTE"
        val rol = runCatching { RolUsuario.valueOf(rolStr) }.getOrDefault(RolUsuario.CLIENTE)
        usuarioActual = UsuarioSesion(id, nombre, email, rol)
        return usuarioActual
    }

    fun cerrarSesion() {
        usuarioActual = null
        prefs.edit().clear().apply()
    }

    fun estaLogueado(): Boolean = obtenerSesion() != null
    fun esCliente(): Boolean = obtenerSesion()?.rol == RolUsuario.CLIENTE
    fun esCocinero(): Boolean = obtenerSesion()?.rol == RolUsuario.COCINERO
    fun esAdmin(): Boolean = obtenerSesion()?.rol in listOf(RolUsuario.ADMIN, RolUsuario.EMPLEADO)
}
