package com.example.viagourmet.data.model

import com.google.gson.annotations.SerializedName

data class LoginEmpleadoResponse(
    @SerializedName("id_empleado") val id: Int,
    @SerializedName("nombre")      val nombre: String,
    @SerializedName("apellido")    val apellido: String,
    @SerializedName("rol")         val rol: String,
    @SerializedName("email")       val email: String?
)