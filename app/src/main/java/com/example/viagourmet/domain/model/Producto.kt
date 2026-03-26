package com.example.viagourmet.domain.model

import java.math.BigDecimal

data class Producto(
    val id: Int,
    val categoriaId: Int,
    val nombre: String,
    val descripcion: String,
    val precio: BigDecimal,
    val disponible: Boolean,
    val imagenUrl: String?,
    val categoria: Categoria? = null
)