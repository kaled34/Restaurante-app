package com.example.viagourmet.data.model

import com.google.gson.annotations.SerializedName
import com.example.viagourmet.domain.model.DetallePedido
import java.math.BigDecimal

data class DetallePedidoDto(
    @SerializedName("id_detalle") val id: Int,
    @SerializedName("id_pedido") val pedidoId: Int,
    @SerializedName("id_producto") val productoId: Int,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precio_unitario") val precioUnitario: BigDecimal,
    @SerializedName("subtotal") val subtotal: BigDecimal,
    @SerializedName("notas") val notas: String?
) {
    fun toDomain(): DetallePedido {
        return DetallePedido(
            id = id,
            pedidoId = pedidoId,
            productoId = productoId,
            cantidad = cantidad,
            precioUnitario = precioUnitario,
            notas = notas,
            producto = null
        )
    }
}