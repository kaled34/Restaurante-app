package com.example.viagourmet.data.model

import com.google.gson.annotations.SerializedName
import com.example.viagourmet.domain.model.Empleado
import com.example.viagourmet.domain.model.RolEmpleado

data class EmpleadoDto(
    @SerializedName("id_empleado") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String,
    @SerializedName("rol") val rol: String,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("activo") val activo: Int,
    @SerializedName("creado_en") val creadoEn: String
) {
    fun toDomain(): Empleado {
        return Empleado(
            id = id,
            nombre = nombre,
            apellido = apellido,
            rol = RolEmpleado.fromString(rol),
            telefono = telefono,
            email = email,
            activo = activo == 1
        )
    }
}