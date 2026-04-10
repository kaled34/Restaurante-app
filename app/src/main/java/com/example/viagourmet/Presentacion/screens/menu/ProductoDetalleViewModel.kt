package com.example.viagourmet.Presentacion.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viagourmet.data.repository.MenuRepositoryImpl
import com.example.viagourmet.data.repository.PedidoRepositoryLocal
import com.example.viagourmet.domain.model.Producto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductoDetalleUiState(
    val producto: Producto? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class ProductoDetalleViewModel @Inject constructor(
    private val repository: PedidoRepositoryLocal,
    private val menuRepositoryImpl: MenuRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductoDetalleUiState())
    val uiState: StateFlow<ProductoDetalleUiState> = _uiState.asStateFlow()

    /**
     * Busca el producto primero en el estado local del MenuRepository
     * (que ya fue sincronizado con la API al iniciar).
     * Si no lo encuentra, lo busca directamente en la API.
     */
    fun cargarProducto(id: Int) {
        viewModelScope.launch {
            _uiState.value = ProductoDetalleUiState(isLoading = true)

            // 1. Buscar en el estado local (ya sincronizado con la API)
            val local = menuRepositoryImpl.getProductoById(id)
            if (local != null) {
                _uiState.value = ProductoDetalleUiState(producto = local, isLoading = false)
                return@launch
            }

            // 2. Si no está en local, buscar en la API directamente
            try {
                val resp = menuRepositoryImpl.buscarProductoPorIdEnApi(id)
                _uiState.value = ProductoDetalleUiState(producto = resp, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = ProductoDetalleUiState(
                    isLoading = false,
                    errorMessage = "Producto no encontrado"
                )
            }
        }
    }

    /**
     * Método legacy para compatibilidad con ProductoDetalleScreen
     * que aún llama a getProductoById de forma síncrona.
     */
    fun getProductoById(id: Int): Producto? = menuRepositoryImpl.getProductoById(id)

    fun agregarAlCarrito(producto: Producto, cantidad: Int) {
        repository.agregarAlCarrito(producto, cantidad)
    }
}