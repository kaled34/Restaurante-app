package com.example.viagourmet.data.repository

import com.example.viagourmet.domain.model.EstadoPedido
import com.example.viagourmet.domain.model.ModuloPedido
import com.example.viagourmet.domain.model.Pedido
import com.example.viagourmet.domain.model.TipoPedido

interface PedidoRepository {
    suspend fun listarPedidos(): List<Pedido>
    suspend fun listarPorEstado(estado: EstadoPedido): List<Pedido>
    suspend fun listarPorCliente(clienteId: Int): List<Pedido>
    suspend fun cambiarEstado(idPedido: Int, estado: EstadoPedido)
    suspend fun crearPedidoLibre(
        clienteId: Int?,
        descripcion: String,
        minutosEntrega: Int,
        cuentaPendiente: Boolean,
        modulo: ModuloPedido,
        tipo: TipoPedido
    )
}
