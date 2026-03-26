package com.example.viagourmet.domain.model

data class Empleado(
    val id: Int,
    val nombre: String,
    val apellido: String,
    val rol: RolEmpleado,
    val telefono: String?,
    val email: String?,
    val activo: Boolean
)

enum class RolEmpleado {
    ADMIN,
    MESERO,
    COCINERO,
    CAJERO;

    companion object {
        fun fromString(value: String): RolEmpleado {
            return when (value.lowercase()) {
                "admin" -> ADMIN
                "mesero" -> MESERO
                "cocinero" -> COCINERO
                "cajero" -> CAJERO
                else -> MESERO
            }
        }
    }
}