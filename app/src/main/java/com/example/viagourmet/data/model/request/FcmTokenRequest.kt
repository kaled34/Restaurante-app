package com.example.viagourmet.data.model.request

import com.google.gson.annotations.SerializedName

data class FcmTokenRequest(
    @SerializedName("token") val token: String
)
