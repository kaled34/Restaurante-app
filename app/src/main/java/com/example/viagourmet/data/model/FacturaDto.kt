package com.example.viagourmet.data.model

import com.google.gson.annotations.SerializedName
import com.example.viagourmet.domain.model.Factura
import com.example.viagourmet.domain.model.MetodoPago
import java.math.BigDecimal
import java.time.LocalDateTime

data class FacturaDto(
    @SerializedName("id_factura") val id: Int,
    @SerializedName("id_pedido") val pedidoId: Int,
    @SerializedName("subtotal") val subtotal: BigDecimal,
    @SerializedName("descuento") val descuento: BigDecimal,
    @SerializedName("impuesto") val impuesto: BigDecimal,
    @SerializedName("total") val total: BigDecimal,
    @SerializedName("metodo_pago") val metodoPago: String,
    @SerializedName("pagado") val pagado: Int,
    @SerializedName("referencia_pago") val referenciaPago: String?,
    @SerializedName("emitida_en") val emitidaEn: String
) {
    fun toDomain(): Factura {
        return Factura(
            id = id,
            pedidoId = pedidoId,
            subtotal = subtotal,
            descuento = descuento,
            impuesto = impuesto,
            total = total,
            metodoPago = MetodoPago.fromString(metodoPago),
            pagado = pagado == 1,
            referenciaPago = referenciaPago,
            emitidaEn = LocalDateTime.parse(emitidaEn.replace(" ", "T")),
            pedido = null
        )
    }
}