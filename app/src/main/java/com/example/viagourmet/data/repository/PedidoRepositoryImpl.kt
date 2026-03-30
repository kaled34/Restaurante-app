package com.example.viagourmet.data.repository

import com.example.viagourmet.data.api.CafeteriaApiService
import com.example.viagourmet.data.dao.PedidoDao
import com.example.viagourmet.data.model.request.CrearPedidoLibreRequest
import com.example.viagourmet.domain.model.EstadoPedido
import com.example.viagourmet.domain.model.ModuloPedido
import com.example.viagourmet.domain.model.Pedido
import com.example.viagourmet.domain.model.TipoPedido
import java.time.LocalDateTime
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
            if (!response.isSuccessful) return emptyList()
            val data = response.body()?.data ?: return emptyList()
            data.map { dto -> dto.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun listarPorEstado(estado: EstadoPedido): List<Pedido> {
        return try {
            // Se usa el nombre del enum para que coincida con lo que espera el servidor
            val response = api.listarPedidosPorEstado(estado.name)
            if (!response.isSuccessful) return emptyList()
            val data = response.body()?.data ?: return emptyList()
            data.map { dto -> dto.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun listarPorCliente(clienteId: Int): List<Pedido> {
        return try {
            val response = api.listarPedidosPorCliente(clienteId)
            if (!response.isSuccessful) return emptyList()
            val data = response.body()?.data ?: return emptyList()
            data.map { dto -> dto.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun cambiarEstado(idPedido: Int, estado: EstadoPedido) {
        val response = api.cambiarEstadoPedido(idPedido, estado.name)
        if (!response.isSuccessful) {
            throw Exception("Error al cambiar estado: ${response.code()} ${response.errorBody()?.string()}")
        }
        
        // Actualizamos también en la base de datos local para mantener sincronía
        val now = LocalDateTime.now().toString()
        dao.actualizarEstado(idPedido, estado.name, now)
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
            modulo = modulo.name,
            tipo = tipo.name,
            minutosEntrega = minutosEntrega,
            metodoPago = if (cuentaPendiente) "cuenta_pendiente" else "efectivo"
            // idEmpleado tiene valor por defecto 1 en el DTO
        )
        val response = api.crearPedidoLibre(req)
        if (!response.isSuccessful) {
            throw Exception("Error al crear pedido libre: ${response.code()} ${response.errorBody()?.string()}")
        }
    }
}
