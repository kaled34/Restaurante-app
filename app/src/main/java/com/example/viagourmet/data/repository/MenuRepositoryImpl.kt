package com.example.viagourmet.data.repository

import com.example.viagourmet.data.api.CafeteriaApiService
import com.example.viagourmet.data.mock.MockData
import com.example.viagourmet.data.model.request.ProductoRequest
import com.example.viagourmet.domain.model.Categoria
import com.example.viagourmet.domain.model.Producto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MenuRepository — gestiona productos y categorías del menú.
 *
 * Se conecta a la API remota para todas las operaciones CRUD.
 * Si no hay conexión, usa MockData como fallback.
 *
 * Usado por:
 *  - EditarMenuViewModel: CRUD de productos (pantalla admin)
 *  - ProductoDetalleViewModel: búsqueda de producto por id
 */
@Singleton
class MenuRepositoryImpl @Inject constructor(
    private val api: CafeteriaApiService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos.asStateFlow()

    private val _categorias = MutableStateFlow<List<Categoria>>(emptyList())
    val categorias: StateFlow<List<Categoria>> = _categorias.asStateFlow()

    init {
        scope.launch { refrescarDesdeApi() }
    }

    // ── Carga desde API con fallback a MockData ───────────────────────────────

    private suspend fun refrescarDesdeApi() {
        try {
            // Cargar categorías
            val respCats = api.listarCategoriasActivas()
            if (respCats.isSuccessful) {
                _categorias.value = respCats.body()?.data?.map { it.toDomain() } ?: emptyList()
            }

            // Cargar todos los productos
            val respProds = api.listarProductos()
            if (respProds.isSuccessful) {
                val cats = _categorias.value
                _productos.value = respProds.body()?.data?.map { dto ->
                    val cat = cats.find { it.id == dto.categoriaId }
                    dto.toDomain().copy(categoria = cat)
                } ?: emptyList()
            }

            // Si la API no devolvió datos, usar MockData
            if (_productos.value.isEmpty()) {
                cargarMockData()
            }
        } catch (e: Exception) {
            cargarMockData()
        }
    }

    private fun cargarMockData() {
        _categorias.value = MockData.categorias
        _productos.value = MockData.productos
    }

    // ── Consultas síncronas (sobre estado local ya cargado) ───────────────────

    fun getCategoriasActivas(): List<Categoria> =
        _categorias.value.filter { it.activo }

    fun getProductoById(id: Int): Producto? =
        _productos.value.find { it.id == id }

    fun getProductosPorCategoria(categoriaId: Int): List<Producto> =
        _productos.value.filter { it.categoriaId == categoriaId && it.disponible }

    // ── Búsqueda directa en la API ────────────────────────────────────────────

    /**
     * Busca un producto por id directamente en la API.
     * Útil cuando el producto aún no está en el estado local.
     */
    suspend fun buscarProductoPorIdEnApi(id: Int): Producto? {
        return try {
            val resp = api.obtenerProducto(id)
            if (resp.isSuccessful && resp.body()?.data != null) {
                val dto = resp.body()!!.data!!
                val cat = _categorias.value.find { it.id == dto.categoriaId }
                dto.toDomain().copy(categoria = cat)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // ── CRUD async — llamados desde EditarMenuViewModel ───────────────────────

    suspend fun agregarProductoApi(
        nombre: String,
        descripcion: String,
        precio: BigDecimal,
        categoriaId: Int,
        imagenUrl: String? = null
    ) {
        val request = ProductoRequest(
            idCategoria = categoriaId,
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            disponible = true,
            imagenUrl = imagenUrl
        )
        val resp = api.crearProducto(request)
        if (resp.isSuccessful && resp.body()?.data != null) {
            val nuevo = resp.body()!!.data!!.toDomain().copy(
                categoria = _categorias.value.find { it.id == categoriaId }
            )
            _productos.value = _productos.value + nuevo
        } else {
            throw Exception("Error al crear producto: ${resp.code()} ${resp.errorBody()?.string()}")
        }
    }

    suspend fun editarProductoApi(
        productoId: Int,
        nombre: String,
        descripcion: String,
        precio: BigDecimal,
        categoriaId: Int,
        disponible: Boolean,
        imagenUrl: String? = null
    ): Boolean {
        val request = ProductoRequest(
            idCategoria = categoriaId,
            nombre = nombre,
            descripcion = descripcion,
            precio = precio,
            disponible = disponible,
            imagenUrl = imagenUrl
        )
        val resp = api.actualizarProducto(productoId, request)
        return if (resp.isSuccessful && resp.body()?.data != null) {
            val actualizado = resp.body()!!.data!!.toDomain().copy(
                categoria = _categorias.value.find { it.id == categoriaId }
            )
            val lista = _productos.value.toMutableList()
            val index = lista.indexOfFirst { it.id == productoId }
            if (index != -1) lista[index] = actualizado
            _productos.value = lista
            true
        } else {
            false
        }
    }

    suspend fun toggleDisponibilidadApi(productoId: Int) {
        val productoActual = getProductoById(productoId) ?: return
        val nuevaDisponibilidad = !productoActual.disponible

        val resp = api.cambiarDisponibilidadProducto(productoId, nuevaDisponibilidad)
        if (resp.isSuccessful) {
            val lista = _productos.value.toMutableList()
            val index = lista.indexOfFirst { it.id == productoId }
            if (index != -1) {
                lista[index] = lista[index].copy(disponible = nuevaDisponibilidad)
                _productos.value = lista
            }
        } else {
            throw Exception("Error al cambiar disponibilidad: ${resp.code()}")
        }
    }

    suspend fun eliminarProductoApi(productoId: Int) {
        val resp = api.eliminarProducto(productoId)
        if (resp.isSuccessful) {
            _productos.value = _productos.value.filter { it.id != productoId }
        } else {
            throw Exception("Error al eliminar producto: ${resp.code()}")
        }
    }

    suspend fun recargar() {
        refrescarDesdeApi()
    }

    // ── Métodos síncronos legacy (usan scope interno) ─────────────────────────
    // Mantienen compatibilidad si alguna parte del código los llama directamente.

    fun agregarProducto(
        nombre: String,
        descripcion: String,
        precio: BigDecimal,
        categoriaId: Int,
        imagenUrl: String? = null
    ) {
        scope.launch {
            try {
                agregarProductoApi(nombre, descripcion, precio, categoriaId, imagenUrl)
            } catch (e: Exception) {
                // Fallback local
                val nuevoId = (_productos.value.maxOfOrNull { it.id } ?: 0) + 1
                val categoria = _categorias.value.find { it.id == categoriaId }
                _productos.value = _productos.value + Producto(
                    id = nuevoId, categoriaId = categoriaId, nombre = nombre,
                    descripcion = descripcion, precio = precio, disponible = true,
                    imagenUrl = imagenUrl, categoria = categoria
                )
            }
        }
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
        scope.launch {
            try {
                editarProductoApi(productoId, nombre, descripcion, precio, categoriaId, disponible, imagenUrl)
            } catch (_: Exception) { }
        }
        return true
    }

    fun toggleDisponibilidad(productoId: Int) {
        scope.launch {
            try {
                toggleDisponibilidadApi(productoId)
            } catch (_: Exception) {
                val lista = _productos.value.toMutableList()
                val index = lista.indexOfFirst { it.id == productoId }
                if (index != -1) {
                    lista[index] = lista[index].copy(disponible = !lista[index].disponible)
                    _productos.value = lista
                }
            }
        }
    }

    fun eliminarProducto(productoId: Int) {
        scope.launch {
            try {
                eliminarProductoApi(productoId)
            } catch (_: Exception) {
                _productos.value = _productos.value.filter { it.id != productoId }
            }
        }
    }
}