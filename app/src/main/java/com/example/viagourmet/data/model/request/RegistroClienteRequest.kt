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
        val email: String,

        /**
         * Contraseña en texto plano. El servidor la hashea con BCrypt antes de guardar.
         * Es necesaria para que el cliente pueda hacer login posterior.
         */
        @SerializedName("contrasena")
        val contrasena: String
    )
}