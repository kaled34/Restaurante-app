package com.example.viagourmet.Presentacion.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viagourmet.data.repository.PedidoRepository
import com.example.viagourmet.data.repository.PedidoRepositoryLocal
import com.example.viagourmet.domain.model.EstadoPedido
import com.example.viagourmet.domain.model.Pedido
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class FiltroAdmin(val label: String) {
    TODOS("Todos"),
    PENDIENTE("Pendientes"),
    EN_PREPARACION("En prep."),
    LISTO("Listos"),
    HISTORIAL("Historial")
}

data class AdminPedidosUiState(
    val isLoading: Boolean = false,
    val pedidos: List<Pedido> = emptyList(),
    val filtroSeleccionado: FiltroAdmin = FiltroAdmin.TODOS,
    val pedidoSeleccionado: Pedido? = null,
    val showDetalle: Boolean = false,
    val isActualizando: Boolean = false,
    val mensajeExito: String? = null,
    val errorMessage: String? = null
) {
    val pedidosFiltrados: List<Pedido>
        get() = when (filtroSeleccionado) {
            FiltroAdmin.TODOS          -> pedidos.filter {
                it.estado != EstadoPedido.ENTREGADO && it.estado != EstadoPedido.CANCELADO
            }
            FiltroAdmin.PENDIENTE      -> pedidos.filter { it.estado == EstadoPedido.PENDIENTE }
            FiltroAdmin.EN_PREPARACION -> pedidos.filter { it.estado == EstadoPedido.EN_PREPARACION }
            FiltroAdmin.LISTO          -> pedidos.filter { it.estado == EstadoPedido.LISTO }
            FiltroAdmin.HISTORIAL      -> pedidos.filter {
                it.estado == EstadoPedido.ENTREGADO || it.estado == EstadoPedido.CANCELADO
            }
        }

    val contadorPendientes: Int    get() = pedidos.count { it.estado == EstadoPedido.PENDIENTE }
    val contadorEnPreparacion: Int get() = pedidos.count { it.estado == EstadoPedido.EN_PREPARACION }
    val contadorListos: Int        get() = pedidos.count { it.estado == EstadoPedido.LISTO }
}

sealed class AdminEvent {
    object Cargar : AdminEvent()
    data class SeleccionarFiltro(val filtro: FiltroAdmin) : AdminEvent()
    data class VerDetalle(val pedido: Pedido) : AdminEvent()
    object CerrarDetalle : AdminEvent()
    data class CambiarEstado(val pedidoId: Int, val nuevoEstado: EstadoPedido) : AdminEvent()
    object LimpiarMensaje : AdminEvent()
}

@HiltViewModel
class AdminPedidosViewModel @Inject constructor(
    private val repository: PedidoRepository,
    // Inyectamos el repositorio local para observar pedidos creados por clientes en Room
    private val repositoryLocal: PedidoRepositoryLocal
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminPedidosUiState(isLoading = true))
    val uiState: StateFlow<AdminPedidosUiState> = _uiState.asStateFlow()

    init {
        // Observar Room en tiempo real para ver pedidos nuevos de clientes al instante
        observarPedidosLocales()
        // También intentar cargar de la API remota
        cargar()
    }

    /**
     * Observa el Flow de Room. Cada vez que un cliente crea un pedido local,
     * este Flow emite y la lista del admin se actualiza automáticamente.
     * Los pedidos locales se FUSIONAN con los de la API (sin duplicar por id).
     */
    private fun observarPedidosLocales() {
        repositoryLocal.getPedidosFlow()
            .onEach { pedidosLocales ->
                val estadoActual = _uiState.value
                // Fusionar: los pedidos locales tienen prioridad si hay mismo id
                val pedidosRemotos = estadoActual.pedidos.filter { remoto ->
                    pedidosLocales.none { local -> local.id == remoto.id }
                }
                val fusionados = (pedidosLocales + pedidosRemotos)
                    .sortedByDescending { it.id }

                _uiState.value = estadoActual.copy(
                    pedidos = fusionados,
                    isLoading = false
                )
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: AdminEvent) {
        when (event) {
            is AdminEvent.Cargar            -> cargar()
            is AdminEvent.SeleccionarFiltro -> _uiState.value = _uiState.value.copy(filtroSeleccionado = event.filtro)
            is AdminEvent.VerDetalle        -> _uiState.value = _uiState.value.copy(pedidoSeleccionado = event.pedido, showDetalle = true)
            is AdminEvent.CerrarDetalle     -> _uiState.value = _uiState.value.copy(showDetalle = false, pedidoSeleccionado = null)
            is AdminEvent.CambiarEstado     -> cambiarEstado(event.pedidoId, event.nuevoEstado)
            is AdminEvent.LimpiarMensaje    -> _uiState.value = _uiState.value.copy(mensajeExito = null, errorMessage = null)
        }
    }

    private fun cargar() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val pedidosApi = repository.listarPedidos()
                if (pedidosApi.isNotEmpty()) {
                    // Fusionar con los locales que ya tenemos en el state
                    val pedidosLocalesActuales = _uiState.value.pedidos.filter { pedido ->
                        pedidosApi.none { api -> api.id == pedido.id }
                    }
                    val fusionados = (pedidosApi + pedidosLocalesActuales)
                        .sortedByDescending { it.id }
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pedidos = fusionados
                    )
                } else {
                    // Si la API no devuelve nada, quedarse con lo que Room ya tiene
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                // Si la API falla (sin red), Room sigue mostrando los pedidos locales
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Sin conexión — mostrando pedidos locales"
                )
            }
        }
    }

    private fun cambiarEstado(pedidoId: Int, nuevoEstado: EstadoPedido) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isActualizando = true)
            try {
                // Actualizar en Room local primero (siempre disponible)
                repositoryLocal.actualizarEstado(pedidoId, nuevoEstado)
                // Intentar también en la API remota
                try { repository.cambiarEstado(pedidoId, nuevoEstado) } catch (_: Exception) {}

                _uiState.value = _uiState.value.copy(
                    isActualizando = false,
                    mensajeExito = "Estado actualizado ✓",
                    showDetalle = false,
                    pedidoSeleccionado = null
                )
                // No es necesario llamar cargar() porque el Flow de Room
                // ya actualizará la lista automáticamente
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isActualizando = false,
                    errorMessage = "Error al cambiar estado: ${e.message}"
                )
            }
        }
    }
}