package com.example.viagourmet.data.model

import com.google.gson.annotations.SerializedName

data class LoginClienteResponse(
    @SerializedName("id_cliente") val id: Int,
    @SerializedName("nombre")     val nombre: String,
    @SerializedName("apellido")   val apellido: String?,
    @SerializedName("telefono")   val telefono: String?,
    @SerializedName("email")      val email: String?
)