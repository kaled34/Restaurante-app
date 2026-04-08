package com.example.viagourmet.data.repository

import com.example.viagourmet.data.mock.MockData
import com.example.viagourmet.domain.model.Categoria
import com.example.viagourmet.domain.model.ModuloCategoria
import com.example.viagourmet.domain.model.Producto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MenuRepository @Inject constructor() {

    // ── Estado reactivo del menú ─────────────────────────────────────────────
    private val _productos = MutableStateFlow(MockData.productos.toMutableList().map { it.copy() })
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    private val _categorias = MutableStateFlow(MockData.categorias.toMutableList().map { it.copy() })
    val categorias: StateFlow<List<Categoria>> = _categorias.asStateFlow()

    // ── Operaciones CRUD de productos ────────────────────────────────────────

    fun agregarProducto(
        nombre: String,
        descripcion: String,
        precio: BigDecimal,
        categoriaId: Int,
        imagenUrl: String? = null
    ): Producto {
        val nuevaId = (_productos.value.maxOfOrNull { it.id } ?: 0) + 1
        val categoria = _categorias.value.find { it.id == categoriaId }
        val nuevo = Producto(
            id          = nuevaId,
            categoriaId = categoriaId,
            nombre      = nombre.trim(),
            descripcion = descripcion.trim(),
            precio      = precio,
            disponible  = true,
            imagenUrl   = imagenUrl,
            categoria   = categoria
        )
        _productos.value = _productos.value + nuevo
        return nuevo
    }

    fun editarProducto(
        productoId: Int,
        nombre: String,
        descripcion: String,
        precio: BigDecimal,
        categoriaId: Int,
        disponible: Boolean,
        imagenUrl: String? = null
    ): Boolean {
        val categoria = _categorias.value.find { it.id == categoriaId }
        val actualizado = _productos.value.map { p ->
            if (p.id == productoId) p.copy(
                nombre      = nombre.trim(),
                descripcion = descripcion.trim(),
                precio      = precio,
                categoriaId = categoriaId,
                disponible  = disponible,
                imagenUrl   = imagenUrl ?: p.imagenUrl,
                categoria   = categoria
            ) else p
        }
        return if (actualizado != _productos.value) {
            _productos.value = actualizado
            true
        } else false
    }

    fun toggleDisponibilidad(productoId: Int): Boolean {
        var cambiado = false
        _productos.value = _productos.value.map { p ->
            if (p.id == productoId) {
                cambiado = true
                p.copy(disponible = !p.disponible)
            } else p
        }
        return cambiado
    }

    fun eliminarProducto(productoId: Int): Boolean {
        val antes = _productos.value.size
        _productos.value = _productos.value.filter { it.id != productoId }
        return _productos.value.size < antes
    }

    // ── Queries de utilidad ──────────────────────────────────────────────────

    fun getProductoById(id: Int): Producto? = _productos.value.find { it.id == id }

    fun getProductosPorModulo(modulo: com.example.viagourmet.domain.model.ModuloPedido): List<Producto> {
        val moduloCat = when (modulo) {
            com.example.viagourmet.domain.model.ModuloPedido.DESAYUNOS -> ModuloCategoria.DESAYUNOS
            com.example.viagourmet.domain.model.ModuloPedido.COMIDAS   -> ModuloCategoria.COMIDAS
            com.example.viagourmet.domain.model.ModuloPedido.LIBRE     -> null
        }
        return if (moduloCat == null) _productos.value
        else _productos.value.filter { it.categoria?.modulo == moduloCat && it.disponible }
    }

    fun getCategoriasActivas(): List<Categoria> = _categorias.value.filter { it.activo }
}