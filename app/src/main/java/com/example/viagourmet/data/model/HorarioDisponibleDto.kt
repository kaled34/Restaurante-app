package com.example.viagourmet.data.model

import com.google.gson.annotations.SerializedName
import com.example.viagourmet.domain.model.HorarioDisponible
import java.time.LocalTime

data class HorarioDisponibleDto(
    @SerializedName("id_horario") val id: Int,
    @SerializedName("hora") val hora: String,
    @SerializedName("etiqueta") val etiqueta: String?,
    @SerializedName("activo") val activo: Int
) {
    fun toDomain(): HorarioDisponible {
        return HorarioDisponible(
            id = id,
            hora = LocalTime.parse(hora),
            etiqueta = etiqueta,
            activo = activo == 1
        )
    }
}