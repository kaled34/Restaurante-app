package com.example.viagourmet.Presentacion.screens.cocinero

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viagourmet.data.repository.PedidoRepository
import com.example.viagourmet.domain.model.EstadoPedido
import com.example.viagourmet.domain.model.Pedido
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CocinerosUiState(
    val pedidos: List<Pedido> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CocinerosViewModel @Inject constructor(
    private val pedidoRepository: PedidoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CocinerosUiState())
    val uiState: StateFlow<CocinerosUiState> = _uiState.asStateFlow()

    // Estados activos que ve el cocinero
    private val estadosActivos = listOf(EstadoPedido.PENDIENTE, EstadoPedido.EN_PREPARACION, EstadoPedido.LISTO)

    init {
        cargarPedidos()
        iniciarPolling()
    }

    private fun cargarPedidos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val todos = pedidoRepository.listarPedidos()
                val activos = todos
                    .filter { it.estado in estadosActivos }
                    .sortedWith(
                        compareBy<Pedido> { estadosActivos.indexOf(it.estado) }
                            .thenBy { it.minutosEntrega ?: Int.MAX_VALUE }
                    )
                _uiState.value = CocinerosUiState(pedidos = activos)
            } catch (e: Exception) {
                _uiState.value = CocinerosUiState(errorMessage = "Error cargando pedidos: ${e.message}")
            }
        }
    }

    /** Auto-refresh cada 30 segundos */
    private fun iniciarPolling() {
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                cargarPedidos()
            }
        }
    }

    fun cambiarEstado(idPedido: Int, nuevoEstado: EstadoPedido) {
        viewModelScope.launch {
            try {
                pedidoRepository.cambiarEstado(idPedido, nuevoEstado)
                cargarPedidos()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error al cambiar estado: ${e.message}")
            }
        }
    }
}
