package com.example.viagourmet.Presentacion.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viagourmet.data.mock.MockData
import com.example.viagourmet.data.repository.MenuRepositoryImpl
import com.example.viagourmet.domain.model.Categoria
import com.example.viagourmet.domain.model.ModuloPedido
import com.example.viagourmet.domain.model.Producto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MenuUiState(
    val isLoading: Boolean = false,
    val categorias: List<Categoria> = emptyList(),
    val productos: List<Producto> = emptyList(),
    val categoriaSeleccionada: Categoria? = null,
    val moduloActual: ModuloPedido = ModuloPedido.DESAYUNOS,
    val errorMessage: String? = null
) {
    val productosFiltrados: List<Producto>
        get() = if (categoriaSeleccionada != null)
            productos.filter { it.categoria?.id == categoriaSeleccionada.id }
        else productos
}

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val menuRepository: MenuRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState(isLoading = true))
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init { cargarMenu(ModuloPedido.DESAYUNOS) }

    fun onModuloChange(modulo: ModuloPedido) {
        cargarMenu(modulo)
    }

    fun onCategoriaChange(categoria: Categoria?) {
        _uiState.value = _uiState.value.copy(categoriaSeleccionada = categoria)
    }

    private fun cargarMenu(modulo: ModuloPedido) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                moduloActual = modulo
            )
            try {
                // Intentar cargar desde la API
                val categorias = menuRepository.obtenerCategoriasPorModulo(modulo)
                val productos  = menuRepository.obtenerProductosPorModulo(modulo)

                if (productos.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        categorias = categorias,
                        productos = productos,
                        categoriaSeleccionada = null
                    )
                } else {
                    // Fallback a MockData si la API no devuelve datos
                    cargarDesdeLocal(modulo)
                }
            } catch (e: Exception) {
                // Sin conexión → usar datos locales
                cargarDesdeLocal(modulo)
            }
        }
    }

    private fun cargarDesdeLocal(modulo: ModuloPedido) {
        val categorias = MockData.getCategoriasActivas()
            .filter { it.modulo == null || it.modulo?.name == modulo.name }
        val productos = MockData.getProductosPorModulo(modulo)
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            categorias = categorias,
            productos = productos,
            categoriaSeleccionada = null,
            errorMessage = if (productos.isEmpty()) "Sin conexión — mostrando datos locales" else null
        )
    }
}