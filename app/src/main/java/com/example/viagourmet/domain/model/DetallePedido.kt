package com.example.viagourmet.domain.model

import java.math.BigDecimal

data class DetallePedido(
    val id: Int,
    val pedidoId: Int,
    val productoId: Int,
    val cantidad: Int,
    val precioUnitario: BigDecimal,
    val notas: String?,
    val producto: Producto? = null
) {
    val subtotal: BigDecimal
        get() = precioUnitario.multiply(BigDecimal(cantidad))
}