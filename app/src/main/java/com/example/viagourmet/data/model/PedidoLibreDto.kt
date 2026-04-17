package com.example.viagourmet.data.model

import com.google.gson.annotations.SerializedName
import com.example.viagourmet.domain.model.PedidoLibre
import java.math.BigDecimal
import java.time.LocalDateTime

data class PedidoLibreDto(
    @SerializedName("id_libre") val id: Int,
    @SerializedName("id_pedido") val pedidoId: Int,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("precio_manual") val precioManual: BigDecimal,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("subtotal") val subtotal: BigDecimal,
    @SerializedName("id_admin") val adminId: Int,
    @SerializedName("notas") val notas: String?,
    @SerializedName("creado_en") val creadoEn: String
) {
    fun toDomain(): PedidoLibre {
        return PedidoLibre(
            id = id,
            pedidoId = pedidoId,
            descripcion = descripcion,
            precioManual = precioManual,
            cantidad = cantidad,
            adminId = adminId,
            notas = notas,
            creadoEn = LocalDateTime.parse(creadoEn.replace(" ", "T")),
            admin = null
        )
    }
}