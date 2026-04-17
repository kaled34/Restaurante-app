package com.example.viagourmet.domain.model

enum class TipoReporte {
    DIARIO,
    SEMANAL,
    MENSUAL,
    ANUAL;

    companion object {
        fun fromString(value: String): TipoReporte {
            return when (value.lowercase()) {
                "diario" -> DIARIO
                "semanal" -> SEMANAL
                "mensual" -> MENSUAL
                "anual" -> ANUAL
                else -> DIARIO
            }
        }
    }
}