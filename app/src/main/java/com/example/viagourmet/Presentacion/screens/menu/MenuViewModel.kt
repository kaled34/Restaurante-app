package com.example.viagourmet.Presentacion.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viagourmet.data.repository.MenuRepository
import com.example.viagourmet.domain.model.Categoria
import com.example.viagourmet.domain.model.ModuloPedido
import com.example.viagourmet.domain.model.ModuloCategoria
import com.example.viagourmet.domain.model.Producto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
    private val menuRepository: MenuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuUiState(isLoading = true))
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private var moduloActual = ModuloPedido.DESAYUNOS

    init {
        // Observar el Flow del repositorio.
        // Cada vez que el admin edite/agregue/elimine un producto,
        // el Flow emite y el cliente ve los cambios al instante.
        menuRepository.productos
            .onEach { actualizarUiParaModulo() }
            .launchIn(viewModelScope)

        actualizarUiParaModulo()
    }

    fun onModuloChange(modulo: ModuloPedido) {
        moduloActual = modulo
        actualizarUiParaModulo()
    }

    fun onCategoriaChange(categoria: Categoria?) {
        _uiState.value = _uiState.value.copy(categoriaSeleccionada = categoria)
    }

    private fun actualizarUiParaModulo() {
        val moduloCat = when (moduloActual) {
            ModuloPedido.DESAYUNOS -> ModuloCategoria.DESAYUNOS
            ModuloPedido.COMIDAS   -> ModuloCategoria.COMIDAS
            ModuloPedido.LIBRE     -> null
        }

        val todosProductos = menuRepository.productos.value
        val productos = if (moduloCat == null) todosProductos
        else todosProductos.filter {
            it.categoria?.modulo == moduloCat && it.disponible
        }

        val categorias = menuRepository.getCategoriasActivas()
            .filter { cat ->
                cat.modulo == null || cat.modulo?.name == moduloActual.name
            }

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            moduloActual = moduloActual,
            productos = productos,
            categorias = categorias,
            categoriaSeleccionada = null
        )
    }
}