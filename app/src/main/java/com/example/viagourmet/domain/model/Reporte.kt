package com.example.viagourmet.domain.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class Reporte(
    val id: Int,
    val tipo: TipoReporte,
    val fechaInicio: LocalDate,
    val fechaFin: LocalDate,
    val totalVentas: BigDecimal,
    val totalPedidos: Int,
    val totalEfectivo: BigDecimal,
    val totalTransferencia: BigDecimal,
    val pedidosDesayunos: Int,
    val pedidosComidas: Int,
    val pedidosLibres: Int,
    val productoTops: List<ProductoTop>, // ✅ AHORA SÍ existe este parámetro
    val generadoPor: Int,
    val generadoEn: LocalDateTime,
    val generadoPorEmpleado: Empleado? = null
)