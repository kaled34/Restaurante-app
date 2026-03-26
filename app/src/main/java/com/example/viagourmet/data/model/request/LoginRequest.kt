package com.example.viagourmet.data.model.request

import com.google.gson.annotations.SerializedName

class LoginRequest {
    data class LoginRequest(
        @SerializedName("email")
        val email: String,

        @SerializedName("contrasena")
        val password: String
    )
}