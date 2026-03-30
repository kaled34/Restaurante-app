package com.example.viagourmet.data.repository

import com.example.viagourmet.data.entity.PedidoEntity
import com.example.viagourmet.data.dao.PedidoDao

import com.example.viagourmet.data.local.mapper.toDetallePedidoEntity
import com.example.viagourmet.data.local.mapper.toDomain
import com.example.viagourmet.data.local.mapper.toEntity
import com.example.viagourmet.domain.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PedidoRepositoryLocal @Inject constructor(
    private val dao: PedidoDao
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Carrito en memoria ───────────────────────────────────────────────────
    private val _carrito = MutableStateFlow<List<ItemCarrito>>(emptyList())
    val carrito: StateFlow<List<ItemCarrito>> = _carrito.asStateFlow()

    // ── Flow de pedidos desde Room ───────────────────────────────────────────
    private val _pedidosFromDb: Flow<List<Pedido>> =
        dao.getAllPedidosFlow()
            .map { lista -> lista.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)

    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos: StateFlow<List<Pedido>> = _pedidos.asStateFlow()

    init {
        scope.launch {
            _pedidosFromDb.collect { lista ->
                _pedidos.value = lista
            }
        }
    }

    fun getPedidosFlow(): StateFlow<List<Pedido>> = pedidos

    // ── Carrito ──────────────────────────────────────────────────────────────

    fun agregarAlCarrito(producto: Producto, cantidad: Int) {
        _carrito.update { lista ->
            val existente = lista.find { it.producto.id == producto.id }
            if (existente != null) {
                lista.map {
                    if (it.producto.id == producto.id)
                        it.copy(cantidad = it.cantidad + cantidad)
                    else it
                }
            } else {
                lista + ItemCarrito(
                    id = System.currentTimeMillis().toInt(),
                    producto = producto,
                    cantidad = cantidad
                )
            }
        }
    }

    fun eliminarDelCarrito(itemId: Int) {
        _carrito.update { lista -> lista.filter { it.id != itemId } }
    }

    fun actualizarCantidadCarrito(itemId: Int, nuevaCantidad: Int) {
        _carrito.update { lista ->
            lista.map { if (it.id == itemId) it.copy(cantidad = nuevaCantidad) else it }
        }
    }

    fun limpiarCarrito() {
        _carrito.value = emptyList()
    }

    // ── Crear pedido ─────────────────────────────────────────────────────────

    /**
     * @param empleadoId  Id del usuario que confirma el pedido (empleado o cliente).
     * @param clienteId   Id del cliente dueño del pedido.
     *                    Si el cliente hace su propio pedido, clienteId == empleadoId.
     *                    Si el empleado crea el pedido, clienteId debe ser el id del
     *                    cliente al que se le asigna (o 0 si no aplica).
     * @param clienteNombre Nombre que se mostrará en el panel admin.
     */
    suspend fun crearPedido(
        empleadoId: Int,
        clienteId: Int,
        clienteNombre: String,
        horario: String?,
        notas: String?
    ): Pedido {
        val itemsActuales = _carrito.value
        val ahora = nowString()

        val notasFinal = buildString {
            if (!horario.isNullOrBlank()) append("🕐 Recogida: $horario")
            if (!notas.isNullOrBlank()) {
                if (isNotBlank()) append(" | ")
                append(notas)
            }
        }.ifBlank { null }

        val entity = PedidoEntity(
            empleadoId = empleadoId,
            clienteId = clienteId,
            clienteNombre = clienteNombre,
            clienteApellido = null,
            clienteTelefono = null,
            clienteEmail = null,
            modulo = detectarModulo(itemsActuales).name,
            estado = EstadoPedido.PENDIENTE.name,
            tipo = TipoPedido.PARA_LLEVAR.name,
            horarioRecogidaId = null,
            notas = notasFinal,
            creadoEn = ahora,
            actualizadoEn = ahora
        )
        val pedidoId = dao.insertPedido(entity).toInt()

        val detalles = itemsActuales.map { it.toDetallePedidoEntity(pedidoId) }
        dao.insertDetalles(detalles)

        limpiarCarrito()

        return dao.getPedidoById(pedidoId)!!.toDomain()
    }

    // ── Actualizar estado ────────────────────────────────────────────────────

    suspend fun actualizarEstado(pedidoId: Int, nuevoEstado: EstadoPedido): Boolean {
        val filas = dao.actualizarEstado(pedidoId, nuevoEstado.name, nowString())
        return filas > 0
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun detectarModulo(items: List<ItemCarrito>): ModuloPedido {
        val categorias = items.mapNotNull { it.producto.categoria?.modulo }
        return when {
            categorias.all { it == ModuloCategoria.DESAYUNOS } -> ModuloPedido.DESAYUNOS
            categorias.all { it == ModuloCategoria.COMIDAS }   -> ModuloPedido.COMIDAS
            else -> ModuloPedido.LIBRE
        }
    }

    private fun nowString(): String {
        val c = java.util.Calendar.getInstance()
        return "%04d-%02d-%02dT%02d:%02d:%02d".format(
            c.get(java.util.Calendar.YEAR),
            c.get(java.util.Calendar.MONTH) + 1,
            c.get(java.util.Calendar.DAY_OF_MONTH),
            c.get(java.util.Calendar.HOUR_OF_DAY),
            c.get(java.util.Calendar.MINUTE),
            c.get(java.util.Calendar.SECOND)
        )
    }
}

data class ItemCarrito(val id: Int, val producto: Producto, val cantidad: Int)
data class ItemPedido(val producto: Producto, val cantidad: Int)