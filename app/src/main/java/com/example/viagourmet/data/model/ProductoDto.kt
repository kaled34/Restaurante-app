package com.example.viagourmet.data.model

import com.google.gson.annotations.SerializedName
import com.example.viagourmet.domain.model.Producto
import java.math.BigDecimal

data class ProductoDto(
    // Corregido: El servidor usa camelCase (idProducto)
    @SerializedName("idProducto") val id: Int,
    
    // Corregido: El servidor usa idCategoria
    @SerializedName("idCategoria") val categoriaId: Int,
    
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("precio") val precio: BigDecimal,
    
    // SOLUCIÓN AL ERROR: Cambiado de Int a Boolean para que coincida con el backend
    @SerializedName("disponible") val disponible: Boolean,
    
    @SerializedName("imagenUrl") val imagenUrl: String?,
    
    // Corregido: El servidor usa creadoEn
    @SerializedName("creadoEn") val creadoEn: String
) {
    fun toDomain(): Producto {
        return Producto(
            id = id,
            categoriaId = categoriaId,
            nombre = nombre,
            descripcion = descripcion ?: "",
            precio = precio,
            disponible = disponible, // Ahora ya es boolean
            imagenUrl = imagenUrl,
            categoria = null
        )
    }
}
