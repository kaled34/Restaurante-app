package com.example.viagourmet.data.model

import com.google.gson.annotations.SerializedName
import com.example.viagourmet.domain.model.Cliente

data class ClienteDto(
    @SerializedName("id_cliente") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellido") val apellido: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("creado_en") val creadoEn: String
) {
    fun toDomain(): Cliente {
        return Cliente(
            id = id,
            nombre = nombre,
            apellido = apellido,
            telefono = telefono,
            email = email
        )
    }
}