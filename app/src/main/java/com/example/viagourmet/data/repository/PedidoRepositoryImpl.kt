package com.example.viagourmet.data.repository

import com.example.viagourmet.data.api.CafeteriaApiService
import com.example.viagourmet.data.local.dao.PedidoDao
import com.example.viagourmet.data.local.mapper.toDomain
import com.example.viagourmet.data.local.mapper.toDetallePedidoEntity
import com.example.viagourmet.data.local.mapper.toEntity
import com.example.viagourmet.data.entity.PedidoEntity
import com.example.viagourmet.data.model.request.CrearPedidoLibreRequest
import com.example.viagourmet.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PedidoRepositoryImpl @Inject constructor(
    private val dao: PedidoDao,
    private val api: CafeteriaApiService
) : PedidoRepository {

    override suspend fun listarPedidos(): List<Pedido> {
        return try {
            val response = api.listarPedidos()
            if (response.isSuccessful) {
                response.body()?.data?.map { it.toDomain() } ?: emptyList()
            } else {
                // Fallback a local
                dao.getAllPedidos().map { it.toDomain() }
            }
        } catch (e: Exception) {
            dao.getAllPedidos().map { it.toDomain() }
        }
    }

    override suspend fun listarPorEstado(estado: EstadoPedido): List<Pedido> {
        return try {
            val response = api.listarPedidosPorEstado(estado.name.lowercase())
            if (response.isSuccessful) {
                response.body()?.data?.map { it.toDomain() } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun listarPorCliente(clienteId: Int): List<Pedido> {
        return try {
            val response = api.listarPedidosPorCliente(clienteId)
            if (response.isSuccessful) {
                response.body()?.data?.map { it.toDomain() } ?: emptyList()
            } else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun cambiarEstado(idPedido: Int, estado: EstadoPedido) {
        val response = api.cambiarEstadoPedido(idPedido, estado.name.lowercase())
        if (!response.isSuccessful) {
            throw Exception("Error al cambiar estado: ${response.code()}")
        }
        // Actualizar local también
        dao.actualizarEstado(idPedido, estado.name, nowString())
    }

    override suspend fun crearPedidoLibre(
        clienteId: Int?,
        descripcion: String,
        minutosEntrega: Int,
        cuentaPendiente: Boolean,
        modulo: ModuloPedido,
        tipo: TipoPedido
    ) {
        val req = CrearPedidoLibreRequest(
            idCliente = clienteId,
            notas = descripcion,
            modulo = modulo.name.lowercase(),
            tipo = tipo.name.lowercase(),
            minutosEntrega = minutosEntrega,
            metodoPago = if (cuentaPendiente) "cuenta_pendiente" else "efectivo"
        )
        val response = api.crearPedidoLibre(req)
        if (!response.isSuccessful) {
            throw Exception("Error al crear pedido libre: ${response.code()}")
        }
    }

    private fun nowString(): String {
        val c = java.util.Calendar.getInstance()
        return "%04d-%02d-%02dT%02d:%02d:%02d".format(
            c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH) + 1,
            c.get(java.util.Calendar.DAY_OF_MONTH), c.get(java.util.Calendar.HOUR_OF_DAY),
            c.get(java.util.Calendar.MINUTE), c.get(java.util.Calendar.SECOND)
        )
    }
}
