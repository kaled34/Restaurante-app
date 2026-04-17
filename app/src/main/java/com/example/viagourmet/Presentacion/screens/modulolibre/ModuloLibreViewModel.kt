package com.example.viagourmet.Presentacion.screens.modulolibre

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viagourmet.Presentacion.session.SessionManager
import com.example.viagourmet.data.repository.PedidoRepository
import com.example.viagourmet.domain.model.ModuloPedido
import com.example.viagourmet.domain.model.TipoPedido
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ModuloLibreUiState(
    val descripcion: String = "",
    val minutosSeleccionados: Int? = null,
    val cuentaPendiente: Boolean = false,
    val isLoading: Boolean = false,
    val pedidoEnviado: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ModuloLibreViewModel @Inject constructor(
    private val pedidoRepository: PedidoRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ModuloLibreUiState())
    val uiState: StateFlow<ModuloLibreUiState> = _uiState.asStateFlow()

    fun onDescripcionChange(value: String) {
        _uiState.value = _uiState.value.copy(descripcion = value, errorMessage = null)
    }

    fun onTiempoSeleccionado(minutos: Int) {
        _uiState.value = _uiState.value.copy(minutosSeleccionados = minutos)
    }

    fun onCuentaPendienteChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(cuentaPendiente = value)
    }

    fun enviarPedido() {
        val state = _uiState.value
        if (state.descripcion.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Describe tu pedido")
            return
        }
        if (state.minutosSeleccionados == null) {
            _uiState.value = state.copy(errorMessage = "Selecciona el tiempo de entrega")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            try {
                val sesion = sessionManager.obtenerSesion()
                pedidoRepository.crearPedidoLibre(
                    clienteId = sesion?.id,
                    descripcion = state.descripcion,
                    minutosEntrega = state.minutosSeleccionados,
                    cuentaPendiente = state.cuentaPendiente,
                    modulo = ModuloPedido.LIBRE,
                    tipo = TipoPedido.PARA_LLEVAR
                )
                _uiState.value = ModuloLibreUiState(pedidoEnviado = true)
            } catch (e: Exception) {
                _uiState.value = state.copy(isLoading = false, errorMessage = "Error: ${e.message}")
            }
        }
    }
}
