package com.example.viagourmet.Presentacion.screens.admin

import androidx.lifecycle.ViewModel
import com.example.viagourmet.data.repository.MenuRepository
import com.example.viagourmet.domain.model.Categoria
import com.example.viagourmet.domain.model.Producto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import java.math.BigDecimal
import javax.inject.Inject

// ── Estados de UI ────────────────────────────────────────────────────────────

data class EditarMenuUiState(
    val productos: List<Producto> = emptyList(),
    val categorias: List<Categoria> = emptyList(),
    val filtroCategoria: Int? = null,           // null = todos
    val filtroBusqueda: String = "",
    val productoSeleccionado: Producto? = null,
    val showFormulario: Boolean = false,
    val isEditing: Boolean = false,             // true = editar, false = agregar
    val mensajeExito: String? = null,
    val errorMessage: String? = null
) {
    val productosFiltrados: List<Producto>
        get() {
            var lista = productos
            if (filtroCategoria != null) lista = lista.filter { it.categoriaId == filtroCategoria }
            if (filtroBusqueda.isNotBlank())
                lista = lista.filter { it.nombre.contains(filtroBusqueda, ignoreCase = true) }
            return lista
        }

    val totalActivos: Int   get() = productos.count { it.disponible }
    val totalInactivos: Int get() = productos.count { !it.disponible }
}

data class FormularioProducto(
    val nombre: String = "",
    val descripcion: String = "",
    val precioTexto: String = "",
    val categoriaId: Int = 1,
    val disponible: Boolean = true,
    val imagenUrl: String = ""
) {
    val precioValido: Boolean
        get() = precioTexto.toBigDecimalOrNull()?.let { it > BigDecimal.ZERO } == true

    val formularioValido: Boolean
        get() = nombre.isNotBlank() && descripcion.isNotBlank() && precioValido
}

sealed class EditarMenuEvent {
    // Filtros
    data class FiltrarCategoria(val categoriaId: Int?) : EditarMenuEvent()
    data class BuscarProducto(val query: String) : EditarMenuEvent()

    // CRUD
    object AbrirNuevoProducto : EditarMenuEvent()
    data class AbrirEditarProducto(val producto: Producto) : EditarMenuEvent()
    object CerrarFormulario : EditarMenuEvent()
    data class GuardarProducto(val form: FormularioProducto) : EditarMenuEvent()
    data class ToggleDisponibilidad(val productoId: Int) : EditarMenuEvent()
    data class EliminarProducto(val productoId: Int) : EditarMenuEvent()

    // Limpiar mensajes
    object LimpiarMensaje : EditarMenuEvent()
}

// ── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class EditarMenuViewModel @Inject constructor(
    private val menuRepository: MenuRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditarMenuUiState())
    val uiState: StateFlow<EditarMenuUiState> = _uiState.asStateFlow()

    init {
        // Observar el repositorio reactivo
        _uiState.value = _uiState.value.copy(
            productos  = menuRepository.productos.value,
            categorias = menuRepository.getCategoriasActivas()
        )
        // Mantener sincronía cuando el Flow del repo emita
        menuRepository.productos.let { flow ->
            _uiState.value = _uiState.value.copy(productos = flow.value)
        }
        refrescarProductos()
    }

    fun onEvent(event: EditarMenuEvent) {
        when (event) {
            is EditarMenuEvent.FiltrarCategoria   -> _uiState.value = _uiState.value.copy(filtroCategoria = event.categoriaId)
            is EditarMenuEvent.BuscarProducto     -> _uiState.value = _uiState.value.copy(filtroBusqueda = event.query)
            is EditarMenuEvent.AbrirNuevoProducto -> _uiState.value = _uiState.value.copy(
                showFormulario = true,
                isEditing = false,
                productoSeleccionado = null
            )
            is EditarMenuEvent.AbrirEditarProducto -> _uiState.value = _uiState.value.copy(
                showFormulario = true,
                isEditing = true,
                productoSeleccionado = event.producto
            )
            is EditarMenuEvent.CerrarFormulario   -> _uiState.value = _uiState.value.copy(
                showFormulario = false,
                productoSeleccionado = null
            )
            is EditarMenuEvent.GuardarProducto    -> guardarProducto(event.form)
            is EditarMenuEvent.ToggleDisponibilidad -> toggleDisponibilidad(event.productoId)
            is EditarMenuEvent.EliminarProducto   -> eliminarProducto(event.productoId)
            is EditarMenuEvent.LimpiarMensaje     -> _uiState.value = _uiState.value.copy(
                mensajeExito = null,
                errorMessage = null
            )
        }
    }

    private fun guardarProducto(form: FormularioProducto) {
        if (!form.formularioValido) {
            _uiState.value = _uiState.value.copy(errorMessage = "Completa todos los campos correctamente")
            return
        }
        val precio = form.precioTexto.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val state  = _uiState.value

        if (state.isEditing && state.productoSeleccionado != null) {
            val exito = menuRepository.editarProducto(
                productoId  = state.productoSeleccionado.id,
                nombre      = form.nombre,
                descripcion = form.descripcion,
                precio      = precio,
                categoriaId = form.categoriaId,
                disponible  = form.disponible,
                imagenUrl   = form.imagenUrl.ifBlank { null }
            )
            if (exito) {
                refrescarProductos()
                _uiState.value = _uiState.value.copy(
                    showFormulario = false,
                    productoSeleccionado = null,
                    mensajeExito = "✅ Producto actualizado"
                )
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "No se pudo actualizar el producto")
            }
        } else {
            menuRepository.agregarProducto(
                nombre      = form.nombre,
                descripcion = form.descripcion,
                precio      = precio,
                categoriaId = form.categoriaId,
                imagenUrl   = form.imagenUrl.ifBlank { null }
            )
            refrescarProductos()
            _uiState.value = _uiState.value.copy(
                showFormulario = false,
                mensajeExito = "✅ Producto agregado al menú"
            )
        }
    }

    private fun toggleDisponibilidad(productoId: Int) {
        menuRepository.toggleDisponibilidad(productoId)
        refrescarProductos()
        val producto = menuRepository.getProductoById(productoId)
        val estado   = if (producto?.disponible == true) "activado" else "desactivado"
        _uiState.value = _uiState.value.copy(mensajeExito = "Producto $estado")
    }

    private fun eliminarProducto(productoId: Int) {
        menuRepository.eliminarProducto(productoId)
        refrescarProductos()
        _uiState.value = _uiState.value.copy(mensajeExito = "🗑 Producto eliminado")
    }

    private fun refrescarProductos() {
        _uiState.value = _uiState.value.copy(
            productos  = menuRepository.productos.value,
            categorias = menuRepository.getCategoriasActivas()
        )
    }
}