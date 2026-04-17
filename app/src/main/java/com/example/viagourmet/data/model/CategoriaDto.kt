package com.example.viagourmet.data.model

import com.google.gson.annotations.SerializedName
import com.example.viagourmet.domain.model.Categoria
import com.example.viagourmet.domain.model.ModuloCategoria

data class CategoriaDto(
    @SerializedName("id_categoria") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("modulo") val modulo: String,
    @SerializedName("activo") val activo: Int,
    @SerializedName("creado_en") val creadoEn: String
) {
    fun toDomain(): Categoria {
        return Categoria(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            modulo = ModuloCategoria.fromString(modulo),
            activo = activo == 1
        )
    }
}