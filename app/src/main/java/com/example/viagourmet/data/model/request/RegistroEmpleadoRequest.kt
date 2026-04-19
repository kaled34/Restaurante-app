package com.example.viagourmet.data.model.request

import com.google.gson.annotations.SerializedName

data class RegistroEmpleadoRequest(
    @SerializedName("nombre")
    val nombre: String,

    @SerializedName("apellido")
    val apellido: String?,

    @SerializedName("email")
    val email: String,

    @SerializedName("contrasena")
    val contrasena: String,

    @SerializedName("rol")
    val rol: String
)
