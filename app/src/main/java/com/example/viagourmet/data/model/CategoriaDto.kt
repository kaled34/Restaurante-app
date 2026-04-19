package com.example.viagourmet.data.model

import com.google.gson.annotations.SerializedName
import com.example.viagourmet.domain.model.Categoria
import com.example.viagourmet.domain.model.ModuloCategoria

data class CategoriaDto(
    // Corregido: AWS devuelve idCategoria (camelCase)
    @SerializedName("idCategoria") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("modulo") val modulo: String,
    // Corregido: AWS suele enviar booleanos para tinyint(1)
    @SerializedName("activo") val activo: Boolean,
    // Corregido: AWS devuelve creadoEn
    @SerializedName("creadoEn") val creadoEn: String
) {
    fun toDomain(): Categoria {
        return Categoria(
            id = id,
            nombre = nombre,
            descripcion = descripcion,
            modulo = ModuloCategoria.fromString(modulo),
            activo = activo
        )
    }
}
