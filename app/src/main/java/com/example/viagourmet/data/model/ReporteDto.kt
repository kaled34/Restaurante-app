package com.example.viagourmet.data.model

import com.google.gson.annotations.SerializedName
import com.example.viagourmet.domain.model.Reporte
import com.example.viagourmet.domain.model.TipoReporte
import com.example.viagourmet.domain.model.ProductoTop
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class ReporteDto(
    @SerializedName("id_reporte") val id: Int,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("fecha_inicio") val fechaInicio: String,
    @SerializedName("fecha_fin") val fechaFin: String,
    @SerializedName("total_ventas") val totalVentas: BigDecimal,
    @SerializedName("total_pedidos") val totalPedidos: Int,
    @SerializedName("total_efectivo") val totalEfectivo: BigDecimal,
    @SerializedName("total_transferencia") val totalTransferencia: BigDecimal,
    @SerializedName("pedidos_desayunos") val pedidosDesayunos: Int,
    @SerializedName("pedidos_comidas") val pedidosComidas: Int,
    @SerializedName("pedidos_libres") val pedidosLibres: Int,
    @SerializedName("producto_top") val productoTop: String?,
    @SerializedName("generado_por") val generadoPor: Int,
    @SerializedName("generado_en") val generadoEn: String
) {
    fun toDomain(): Reporte {
        val productoTops = productoTop?.let {
            val parts = it.split(":")
            if (parts.size == 2) {
                listOf(
                    ProductoTop(
                        nombre = parts[0],
                        cantidadVendida = parts[1].toIntOrNull() ?: 0
                    )
                )
            } else {
                emptyList()
            }
        } ?: emptyList()

        return Reporte(
            id = id,
            tipo = TipoReporte.fromString(tipo),
            fechaInicio = LocalDate.parse(fechaInicio),
            fechaFin = LocalDate.parse(fechaFin),
            totalVentas = totalVentas,
            totalPedidos = totalPedidos,
            totalEfectivo = totalEfectivo,
            totalTransferencia = totalTransferencia,
            pedidosDesayunos = pedidosDesayunos,
            pedidosComidas = pedidosComidas,
            pedidosLibres = pedidosLibres,
            productoTops = productoTops, // ✅ AHORA SÍ existe en el modelo de dominio
            generadoPor = generadoPor,
            generadoEn = LocalDateTime.parse(generadoEn.replace(" ", "T")),
            generadoPorEmpleado = null
        )
    }
}