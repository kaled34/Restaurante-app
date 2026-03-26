package com.example.viagourmet.data.model

import com.google.gson.annotations.SerializedName
import com.example.viagourmet.domain.model.*
import java.time.LocalDateTime

data class PedidoDto(
    @SerializedName("id_pedido") val id: Int,
    @SerializedName("id_empleado") val empleadoId: Int,
    @SerializedName("id_cliente") val clienteId: Int?,
    @SerializedName("modulo") val modulo: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("id_horario_recogida") val horarioRecogidaId: Int?,
    @SerializedName("notas") val notas: String?,
    @SerializedName("creado_en") val creadoEn: String,
    @SerializedName("actualizado_en") val actualizadoEn: String,
    @SerializedName("detalles") val detalles: List<DetallePedidoDto>?,
    @SerializedName("items_libres") val itemsLibres: List<PedidoLibreDto>?,
    @SerializedName("cliente") val cliente: ClienteDto?,
    @SerializedName("factura") val factura: FacturaDto?
) {
    fun toDomain(): Pedido {
        return Pedido(
            id = id,
            empleadoId = empleadoId,
            clienteId = clienteId,
            modulo = ModuloPedido.fromString(modulo),
            estado = EstadoPedido.fromString(estado),
            tipo = TipoPedido.fromString(tipo),
            horarioRecogidaId = horarioRecogidaId,
            notas = notas,
            creadoEn = LocalDateTime.parse(creadoEn.replace(" ", "T")),
            actualizadoEn = LocalDateTime.parse(actualizadoEn.replace(" ", "T")),
            detalles = detalles?.map { it.toDomain() } ?: emptyList(),
            itemsLibres = itemsLibres?.map { it.toDomain() } ?: emptyList(),
            empleado = null,
            cliente = cliente?.toDomain(),
            factura = factura?.toDomain()
        )
    }
}