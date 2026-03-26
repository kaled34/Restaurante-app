package com.example.viagourmet.data.model.Response

import com.google.gson.annotations.SerializedName
import com.example.viagourmet.data.model.ClienteDto

class AuthResponse {
    data class AuthResponse(
        @SerializedName("token")
        val token: String,

        @SerializedName("tipo_token")
        val tokenType: String,

        @SerializedName("usuario")
        val usuario: ClienteDto,

        @SerializedName("expira_en")
        val expiraEn: Long
    )
}