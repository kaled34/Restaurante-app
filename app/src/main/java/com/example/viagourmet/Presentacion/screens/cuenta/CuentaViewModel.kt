package com.example.viagourmet.Presentacion.screens.cuenta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viagourmet.Presentacion.session.RolUsuario
import com.example.viagourmet.Presentacion.session.SessionManager
import com.example.viagourmet.data.repository.ItemCarrito
import com.example.viagourmet.data.repository.PedidoRepositoryLocal
import com.example.viagourmet.domain.model.Pedido
import com.example.viagourmet.domain.model.Producto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

enum class OpcionHorario(val label: String, val minutos: Int) {
    AHORA("Ahora mismo", 0),
    MINUTOS_15("15 minutos", 15),
    MINUTOS_30("30 minutos", 30),
    MINUTOS_45("45 minutos", 45),
    UNA_HORA("1 hora", 60)
}

data class CuentaUiState(
    val items: List<ItemCarrito> = emptyList(),
    val subtotal: BigDecimal = BigDecimal.ZERO,
    val iva: BigDecimal = BigDecimal.ZERO,
    val total: BigDecimal = BigDecimal.ZERO,
    val horaSeleccionada: OpcionHorario? = null,
    val isLoading: Boolean = false,
    val pedidoConfirmado: Pedido? = null,
    val mensajeExito: String? = null,
    val errorMessage: String? = null
)

sealed class CuentaEvent {
    data class AgregarProducto(val producto: Producto, val cantidad: Int) : CuentaEvent()
    data class EliminarItem(val itemId: Int) : CuentaEvent()
    data class ActualizarCantidad(val itemId: Int, val nuevaCantidad: Int) : CuentaEvent()
    data class SeleccionarHorario(val opcion: OpcionHorario) : CuentaEvent()
    object ConfirmarPedido : CuentaEvent()
    object LimpiarCuenta : CuentaEvent()
    object LimpiarMensaje : CuentaEvent()
}

@HiltViewModel
class CuentaViewModel @Inject constructor(
    private val repository: PedidoRepositoryLocal,
    val sessionManager: SessionManager // Cambiado a public para acceso desde la vista
) : ViewModel() {

    private val _uiState = MutableStateFlow(CuentaUiState())
    val uiState: StateFlow<CuentaUiState> = _uiState.asStateFlow()

    private val IVA_RATE = BigDecimal("0.16")

    init {
        repository.carrito
            .onEach { items ->
                val subtotal = items.sumOf { it.producto.precio.multiply(BigDecimal(it.cantidad)) }
                val iva = subtotal.multiply(IVA_RATE)
                _uiState.value = _uiState.value.copy(
                    items = items,
                    subtotal = subtotal,
                    iva = iva,
                    total = subtotal + iva
                )
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: CuentaEvent) {
        when (event) {
            is CuentaEvent.AgregarProducto ->
                repository.agregarAlCarrito(event.producto, event.cantidad)
            is CuentaEvent.EliminarItem ->
                repository.eliminarDelCarrito(event.itemId)
            is CuentaEvent.ActualizarCantidad ->
                repository.actualizarCantidadCarrito(event.itemId, event.nuevaCantidad)
            is CuentaEvent.SeleccionarHorario ->
                _uiState.value = _uiState.value.copy(horaSeleccionada = event.opcion)
            is CuentaEvent.ConfirmarPedido -> confirmarPedido()
            is CuentaEvent.LimpiarCuenta -> repository.limpiarCarrito()
            is CuentaEvent.LimpiarMensaje ->
                _uiState.value = _uiState.value.copy(mensajeExito = null, errorMessage = null)
        }
    }

    private fun confirmarPedido() {
        viewModelScope.launch {
            if (_uiState.value.items.isEmpty()) {
                _uiState.value = _uiState.value.copy(errorMessage = "Agrega al menos un producto")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val sesion = sessionManager.obtenerSesion()
                val sesionId = sesion?.id ?: 0
                val sesionNombre = sesion?.nombre ?: "Cliente"


                val esCliente = sesion?.rol == RolUsuario.CLIENTE
                val clienteId = if (esCliente) sesionId else 0

                val pedido = repository.crearPedido(
                    empleadoId = sesionId,
                    clienteId = clienteId,      // ← el cliente real
                    clienteNombre = sesionNombre,
                    horario = _uiState.value.horaSeleccionada?.label,
                    notas = null
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    pedidoConfirmado = pedido,
                    mensajeExito = "✅ Pedido #${pedido.id} confirmado"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al guardar el pedido: ${e.message}"
                )
            }
        }
    }
}
