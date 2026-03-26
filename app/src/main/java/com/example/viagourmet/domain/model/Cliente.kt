package com.example.viagourmet.domain.model

data class Cliente(
    val id: Int,
    val nombre: String,
    val apellido: String?,
    val telefono: String?,
    val email: String?
)