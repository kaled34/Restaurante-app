package com.example.viagourmet.Presentacion.screens.mipedido

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viagourmet.Presentacion.session.SessionManager
import com.example.viagourmet.data.repository.PedidoRepository
import com.example.viagourmet.data.repository.PedidoRepositoryLocal
import com.example.viagourmet.domain.model.EstadoPedido
import com.example.viagourmet.domain.model.Pedido
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MiPedidoUiState(
    val pedidoActivo: Pedido? = null,
    val ultimoEntregado: Pedido? = null,
    val isLoading: Boolean = false,
    val noPedidoActivo: Boolean = false
)

@HiltViewModel
class MiPedidoViewModel @Inject constructor(
    private val repositoryLocal: PedidoRepositoryLocal,
    private val repositoryApi: PedidoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MiPedidoUiState(isLoading = true))
    val uiState: StateFlow<MiPedidoUiState> = _uiState.asStateFlow()

    init {
        // Observar Room en tiempo real (actualiza cuando cambia el estado local)
        observarPedidosLocales()
        // Cargar desde la API para tener datos frescos
        cargarDesdeApi()
        // Polling cada 15 segundos para actualizaciones de estado
        iniciarPolling()
    }

    private fun observarPedidosLocales() {
        val clienteId = sessionManager.obtenerSesion()?.id ?: return

        repositoryLocal.getPedidosFlow()
            .onEach { pedidos ->
                val misPedidos = pedidos.filter { it.clienteId == clienteId }
                actualizarUiState(misPedidos)
            }
            .launchIn(viewModelScope)
    }

    private fun cargarDesdeApi() {
        val clienteId = sessionManager.obtenerSesion()?.id ?: return

        viewModelScope.launch {
            try {
                val pedidosApi = repositoryApi.listarPorCliente(clienteId)
                if (pedidosApi.isNotEmpty()) {
                    actualizarUiState(pedidosApi)
                }
            } catch (_: Exception) {
                // Sin red → Room ya tiene los datos locales
            }
        }
    }

    private fun iniciarPolling() {
        viewModelScope.launch {
            while (true) {
                delay(15_000)
                cargarDesdeApi()
            }
        }
    }

    private fun actualizarUiState(pedidos: List<Pedido>) {
        val activo = pedidos
            .filter {
                it.estado != EstadoPedido.ENTREGADO &&
                        it.estado != EstadoPedido.CANCELADO
            }
            .maxByOrNull { it.id }

        val ultimoEntregado = pedidos
            .filter { it.estado == EstadoPedido.ENTREGADO }
            .maxByOrNull { it.id }

        _uiState.value = MiPedidoUiState(
            pedidoActivo     = activo,
            ultimoEntregado  = ultimoEntregado,
            isLoading        = false,
            noPedidoActivo   = activo == null
        )
    }
}