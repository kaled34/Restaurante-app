package com.example.viagourmet.domain.model

import java.time.LocalTime

data class HorarioDisponible(
    val id: Int,
    val hora: LocalTime,
    val etiqueta: String?,
    val activo: Boolean
)