package com.example.viagourmet.data.model

import com.google.gson.annotations.SerializedName
import com.example.viagourmet.domain.model.Producto
import java.math.BigDecimal

data class ProductoDto(
    @SerializedName("id_producto") val id: Int,
    @SerializedName("id_categoria") val categoriaId: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("precio") val precio: BigDecimal,
    @SerializedName("disponible") val disponible: Int,
    @SerializedName("imagen_url") val imagenUrl: String?,
    @SerializedName("creado_en") val creadoEn: String
) {
    fun toDomain(): Producto {
        return Producto(
            id = id,
            categoriaId = categoriaId,
            nombre = nombre,
            descripcion = descripcion ?: "",
            precio = precio,
            disponible = disponible == 1,
            imagenUrl = imagenUrl,
            categoria = null
        )
    }
}