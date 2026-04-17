package com.example.viagourmet.data.repository

import com.example.viagourmet.data.api.CafeteriaApiService
import com.example.viagourmet.data.dao.PedidoDao
import com.example.viagourmet.data.entity.PedidoEntity
import com.example.viagourmet.data.Local.mapper.toDetallePedidoEntity
import com.example.viagourmet.data.Local.mapper.toDomain
import com.example.viagourmet.data.model.request.CrearPedidoRequest
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
    private val dao: PedidoDao,
    private val api: CafeteriaApiService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Carrito en memoria ───────────────────────────────────────────────────
    private val _carrito = MutableStateFlow<List<ItemCarrito>>(emptyList())
    val carrito: StateFlow<List<ItemCarrito>> = _carrito.asStateFlow()

    // ── Flow de pedidos desde Room ───────────────────────────────────────────
    private val _pedidos = MutableStateFlow<List<Pedido>>(emptyList())
    val pedidos: StateFlow<List<Pedido>> = _pedidos.asStateFlow()

    init {
        scope.launch {
            dao.getAllPedidosFlow()
                .map { lista -> lista.map { it.toDomain() } }
                .flowOn(Dispatchers.IO)
                .collect { _pedidos.value = it }
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

    // ── Crear pedido — envía a la API y guarda en Room ───────────────────────

    /**
     * Crea el pedido en la API remota.
     * Si la API responde OK, guarda el pedido en Room para acceso offline.
     * Si la API falla (sin red), guarda solo en Room como fallback.
     *
     * @param empleadoId  Id del usuario que confirma (cliente o empleado)
     * @param clienteId   Id del cliente dueño del pedido
     * @param clienteNombre Nombre para mostrar en el panel admin
     */
    suspend fun crearPedido(
        empleadoId: Int,
        clienteId: Int,
        clienteNombre: String,
        horario: String?,
        notas: String?
    ): Pedido {
        val items = _carrito.value
        val ahora = nowString()

        val notasFinal = buildString {
            if (!horario.isNullOrBlank()) append("Recogida: $horario")
            if (!notas.isNullOrBlank()) {
                if (isNotBlank()) append(" | ")
                append(notas)
            }
        }.ifBlank { null }

        val modulo = detectarModulo(items)

        // ── Intentar crear en la API ─────────────────────────────────────────
        return try {
            val detallesRequest = items.map { item ->
                CrearPedidoRequest.DetallePedidoRequest(
                    productoId = item.producto.id,
                    cantidad   = item.cantidad,
                    notas      = null
                )
            }

            val request = CrearPedidoRequest.CrearPedidoRequest(
                empleadoId       = DEFAULT_EMPLEADO_ID, // la API requiere un empleado válido
                clienteId        = if (clienteId > 0) clienteId else null,
                modulo           = modulo.toApiString(),
                tipo             = TipoPedido.PARA_LLEVAR.toApiString(),
                horarioRecogidaId = null,
                notas            = notasFinal,
                detalles         = detallesRequest
            )

            val resp = api.crearPedido(request)

            if (resp.isSuccessful && resp.body()?.data != null) {
                // API exitosa → guardar en Room con el id real del servidor
                val pedidoApi = resp.body()!!.data!!
                guardarEnRoom(
                    id            = pedidoApi.id,
                    empleadoId    = empleadoId,
                    clienteId     = clienteId,
                    clienteNombre = clienteNombre,
                    modulo        = modulo,
                    notasFinal    = notasFinal,
                    ahora         = ahora,
                    items         = items
                )
                limpiarCarrito()
                dao.getPedidoById(pedidoApi.id)!!.toDomain()
            } else {
                // API respondió con error → fallback a Room local
                crearEnRoomLocal(empleadoId, clienteId, clienteNombre,
                    modulo, notasFinal, ahora, items)
            }
        } catch (e: Exception) {
            // Sin red → guardar en Room local
            crearEnRoomLocal(empleadoId, clienteId, clienteNombre,
                modulo, notasFinal, ahora, items)
        }
    }

    // ── Actualizar estado ────────────────────────────────────────────────────

    suspend fun actualizarEstado(pedidoId: Int, nuevoEstado: EstadoPedido): Boolean {
        val filas = dao.actualizarEstado(pedidoId, nuevoEstado.name, nowString())
        return filas > 0
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private suspend fun crearEnRoomLocal(
        empleadoId: Int,
        clienteId: Int,
        clienteNombre: String,
        modulo: ModuloPedido,
        notasFinal: String?,
        ahora: String,
        items: List<ItemCarrito>
    ): Pedido {
        val entity = PedidoEntity(
            empleadoId     = empleadoId,
            clienteId      = clienteId,
            clienteNombre  = clienteNombre,
            clienteApellido = null,
            clienteTelefono = null,
            clienteEmail   = null,
            modulo         = modulo.name,
            estado         = EstadoPedido.PENDIENTE.name,
            tipo           = TipoPedido.PARA_LLEVAR.name,
            horarioRecogidaId = null,
            notas          = notasFinal,
            creadoEn       = ahora,
            actualizadoEn  = ahora
        )
        val pedidoId = dao.insertPedido(entity).toInt()
        dao.insertDetalles(items.map { it.toDetallePedidoEntity(pedidoId) })
        limpiarCarrito()
        return dao.getPedidoById(pedidoId)!!.toDomain()
    }

    private suspend fun guardarEnRoom(
        id: Int,
        empleadoId: Int,
        clienteId: Int,
        clienteNombre: String,
        modulo: ModuloPedido,
        notasFinal: String?,
        ahora: String,
        items: List<ItemCarrito>
    ) {
        val entity = PedidoEntity(
            id             = id,
            empleadoId     = empleadoId,
            clienteId      = clienteId,
            clienteNombre  = clienteNombre,
            clienteApellido = null,
            clienteTelefono = null,
            clienteEmail   = null,
            modulo         = modulo.name,
            estado         = EstadoPedido.PENDIENTE.name,
            tipo           = TipoPedido.PARA_LLEVAR.name,
            horarioRecogidaId = null,
            notas          = notasFinal,
            creadoEn       = ahora,
            actualizadoEn  = ahora
        )
        dao.insertPedido(entity)
        dao.insertDetalles(items.map { it.toDetallePedidoEntity(id) })
    }

    private fun detectarModulo(items: List<ItemCarrito>): ModuloPedido {
        val categorias = items.mapNotNull { it.producto.categoria?.modulo }
        return when {
            categorias.all { it == ModuloCategoria.DESAYUNOS } -> ModuloPedido.DESAYUNOS
            categorias.all { it == ModuloCategoria.COMIDAS }   -> ModuloPedido.COMIDAS
            else -> ModuloPedido.LIBRE
        }
    }

    private fun ModuloPedido.toApiString() = when (this) {
        ModuloPedido.DESAYUNOS -> "desayunos"
        ModuloPedido.COMIDAS   -> "comidas"
        ModuloPedido.LIBRE     -> "libre"
    }

    private fun TipoPedido.toApiString() = when (this) {
        TipoPedido.PARA_LLEVAR -> "para_llevar"
        TipoPedido.OFICINA     -> "oficina"
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

    companion object {
        const val DEFAULT_EMPLEADO_ID = 1
    }
}

data class ItemCarrito(val id: Int, val producto: Producto, val cantidad: Int)
data class ItemPedido(val producto: Producto, val cantidad: Int)