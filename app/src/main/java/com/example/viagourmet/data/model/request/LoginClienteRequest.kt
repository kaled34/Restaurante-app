package com.example.viagourmet.data.model.request

import com.google.gson.annotations.SerializedName

class LoginClienteRequest {
    data class LoginClienteRequest(
        @SerializedName("email")
        val email: String,

        @SerializedName("contrasena")
        val contrasena: String
    )
}