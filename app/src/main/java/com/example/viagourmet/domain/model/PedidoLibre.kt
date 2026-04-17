package com.example.viagourmet.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class PedidoLibre(
    val id: Int,
    val pedidoId: Int,
    val descripcion: String,
    val precioManual: BigDecimal,
    val cantidad: Int,
    val adminId: Int,
    val notas: String?,
    val creadoEn: LocalDateTime,
    val admin: Empleado? = null
) {
    val subtotal: BigDecimal
        get() = precioManual.multiply(BigDecimal(cantidad))
}