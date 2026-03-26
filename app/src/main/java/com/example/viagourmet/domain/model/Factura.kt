package com.example.viagourmet.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime

data class Factura(
    val id: Int,
    val pedidoId: Int,
    val subtotal: BigDecimal,
    val descuento: BigDecimal,
    val impuesto: BigDecimal,
    val total: BigDecimal,
    val metodoPago: MetodoPago,
    val pagado: Boolean,
    val referenciaPago: String?,
    val emitidaEn: LocalDateTime,
    val pedido: Pedido? = null
)

enum class MetodoPago {
    EFECTIVO,
    TRANSFERENCIA;

    companion object {
        fun fromString(value: String): MetodoPago {
            return when (value.lowercase()) {
                "efectivo" -> EFECTIVO
                "transferencia" -> TRANSFERENCIA
                else -> EFECTIVO
            }
        }
    }
}