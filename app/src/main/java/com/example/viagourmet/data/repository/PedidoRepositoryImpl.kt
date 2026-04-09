package com.example.viagourmet.data.repository

import com.example.viagourmet.data.api.CafeteriaApiService
import com.example.viagourmet.data.dao.PedidoDao
import com.example.viagourmet.data.model.request.CrearPedidoRequest
import com.example.viagourmet.data.model.request.CrearPedidoLibreItemRequest
import com.example.viagourmet.domain.model.EstadoPedido
import com.example.viagourmet.domain.model.ModuloPedido
import com.example.viagourmet.domain.model.Pedido
import com.example.viagourmet.domain.model.TipoPedido
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PedidoRepositoryImpl — conecta con la API remota (Spring Boot).
 *
 * Mapeo de estados entre la app Kotlin y la API Java:
 *   App (MAYÚSCULAS)  →  API (snake_case minúsculas)
 *   PENDIENTE         →  pendiente
 *   EN_PREPARACION    →  en_preparacion
 *   LISTO             →  listo
 *   ENTREGADO         →  entregado
 *   CANCELADO         →  cancelado
 *
 * Mapeo de módulos:
 *   DESAYUNOS  →  desayunos
 *   COMIDAS    →  comidas
 *   LIBRE      →  libre
 *
 * Mapeo de tipos de pedido:
 *   PARA_LLEVAR  →  para_llevar
 *   OFICINA      →  oficina
 */
@Singleton
class PedidoRepositoryImpl @Inject constructor(
    private val dao: PedidoDao,
    private val api: CafeteriaApiService
) : PedidoRepository {

    // ── Listar pedidos ────────────────────────────────────────────────────────

    override suspend fun listarPedidos(): List<Pedido> {
        return try {
            val response = api.listarPedidos()
            if (!response.isSuccessful) return emptyList()
            val data = response.body()?.data ?: return emptyList()
            data.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun listarPorEstado(estado: EstadoPedido): List<Pedido> {
        return try {
            val response = api.listarPedidos(estado = estado.toApiString())
            if (!response.isSuccessful) return emptyList()
            val data = response.body()?.data ?: return emptyList()
            data.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun listarPorCliente(clienteId: Int): List<Pedido> {
        return try {
            val response = api.listarPedidosPorCliente(clienteId)
            if (!response.isSuccessful) return emptyList()
            val data = response.body()?.data ?: return emptyList()
            data.map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ── Cambiar estado ────────────────────────────────────────────────────────

    override suspend fun cambiarEstado(idPedido: Int, estado: EstadoPedido) {
        val response = api.cambiarEstadoPedido(idPedido, estado.toApiString())
        if (!response.isSuccessful) {
            throw Exception("Error al cambiar estado: ${response.code()} ${response.errorBody()?.string()}")
        }
        // Sincronizar también en Room local
        val now = LocalDateTime.now().toString()
        dao.actualizarEstado(idPedido, estado.name, now)
    }

    // ── Crear pedido estándar (desayunos / comidas) ───────────────────────────

    /**
     * Crea un pedido completo en la API.
     * El request incluye los detalles (productos + cantidades + precio_unitario).
     *
     * IMPORTANTE: el precio_unitario de cada detalle debe enviarse porque
     * la API almacena el precio vigente al momento del pedido.
     */
    suspend fun crearPedidoEnApi(
        empleadoId: Int,
        clienteId: Int?,
        modulo: ModuloPedido,
        tipo: TipoPedido,
        notas: String?,
        detalles: List<CrearPedidoRequest.DetallePedidoRequest>
    ): Pedido {
        val request = CrearPedidoRequest.CrearPedidoRequest(
            empleadoId = empleadoId,
            clienteId = clienteId,
            modulo = modulo.toApiString(),
            tipo = tipo.toApiString(),
            horarioRecogidaId = null,
            notas = notas,
            detalles = detalles
        )
        val response = api.crearPedido(request)
        if (!response.isSuccessful || response.body()?.data == null) {
            throw Exception(
                "Error al crear pedido: ${response.code()} ${response.errorBody()?.string()}"
            )
        }
        return response.body()!!.data!!.toDomain()
    }

    // ── Crear pedido libre ────────────────────────────────────────────────────

    /**
     * Flujo de pedido libre:
     * 1. Crear la cabecera del pedido en la API (modulo=libre).
     * 2. Crear el item libre con descripción y precio manual.
     *
     * La API usa el empleado con id=1 como admin por defecto para pedidos
     * iniciados desde la app cliente (campo id_admin en pedidos_libres).
     */
    override suspend fun crearPedidoLibre(
        clienteId: Int?,
        descripcion: String,
        minutosEntrega: Int,
        cuentaPendiente: Boolean,
        modulo: ModuloPedido,
        tipo: TipoPedido
    ) {
        // Paso 1: Crear cabecera del pedido
        val cabecera = CrearPedidoRequest.CrearPedidoRequest(
            empleadoId = DEFAULT_EMPLEADO_ID,
            clienteId = clienteId,
            modulo = modulo.toApiString(),
            tipo = tipo.toApiString(),
            horarioRecogidaId = null,
            notas = "Pedido libre — $minutosEntrega min — ${if (cuentaPendiente) "cuenta pendiente" else "efectivo"}",
            detalles = emptyList()
        )
        val pedidoResp = api.crearPedido(cabecera)
        if (!pedidoResp.isSuccessful || pedidoResp.body()?.data == null) {
            throw Exception(
                "Error al crear pedido libre: ${pedidoResp.code()} ${pedidoResp.errorBody()?.string()}"
            )
        }
        val pedidoId = pedidoResp.body()!!.data!!.id

        // Paso 2: Agregar el item libre
        val itemResp = api.crearItemPedidoLibre(
            CrearPedidoLibreItemRequest(
                idPedido = pedidoId,
                descripcion = descripcion,
                precioManual = BigDecimal("0.00"), // precio a definir por el admin en caja
                cantidad = 1,
                idAdmin = DEFAULT_EMPLEADO_ID,
                notas = if (cuentaPendiente) "Cuenta pendiente" else null
            )
        )
        if (!itemResp.isSuccessful) {
            throw Exception(
                "Error al crear item libre: ${itemResp.code()} ${itemResp.errorBody()?.string()}"
            )
        }
    }

    // ── Extensiones de mapeo ──────────────────────────────────────────────────

    /** Convierte el enum de la app al string que espera la API de Java. */
    private fun EstadoPedido.toApiString(): String = when (this) {
        EstadoPedido.PENDIENTE      -> "pendiente"
        EstadoPedido.EN_PREPARACION -> "en_preparacion"
        EstadoPedido.LISTO          -> "listo"
        EstadoPedido.ENTREGADO      -> "entregado"
        EstadoPedido.CANCELADO      -> "cancelado"
    }

    private fun ModuloPedido.toApiString(): String = when (this) {
        ModuloPedido.DESAYUNOS -> "desayunos"
        ModuloPedido.COMIDAS   -> "comidas"
        ModuloPedido.LIBRE     -> "libre"
    }

    private fun TipoPedido.toApiString(): String = when (this) {
        TipoPedido.PARA_LLEVAR -> "para_llevar"
        TipoPedido.OFICINA     -> "oficina"
    }

    companion object {
        /** ID del empleado/admin por defecto para pedidos de clientes. */
        const val DEFAULT_EMPLEADO_ID = 1
    }
}