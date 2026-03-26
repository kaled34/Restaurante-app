package com.example.viagourmet.data.model.request

import com.google.gson.annotations.SerializedName

class RegistroClienteRequest {
    data class RegistroClienteRequest(
        @SerializedName("nombre")
        val nombre: String,

        @SerializedName("apellido")
        val apellido: String?,

        @SerializedName("telefono")
        val telefono: String?,

        @SerializedName("email")
        val email: String
    )
}