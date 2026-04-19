package com.example.viagourmet.data.repository

import android.util.Log
import com.example.viagourmet.data.api.CafeteriaApiService
import com.example.viagourmet.data.model.request.ProductoRequest
import com.example.viagourmet.domain.model.Categoria
import com.example.viagourmet.domain.model.ModuloPedido
import com.example.viagourmet.domain.model.ModuloCategoria
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

    suspend fun refrescarDesdeApi() {
        try {
            // Cargar categorías reales de AWS
            val respCats = api.listarCategoriasActivas()
            if (respCats.isSuccessful) {
                _categorias.value = respCats.body()?.data?.map { it.toDomain() } ?: emptyList()
            }

            // Cargar productos reales de AWS (Todos, sin filtros iniciales)
            val respProds = api.listarProductos()
            if (respProds.isSuccessful) {
                val cats = _categorias.value
                val prodsApi = respProds.body()?.data?.map { dto ->
                    val cat = cats.find { it.id == dto.categoriaId }
                    // Si no encuentra la categoría, aún así creamos el producto para que no sea invisible
                    dto.toDomain().copy(categoria = cat)
                } ?: emptyList()
                
                _productos.value = prodsApi
                Log.d("MenuRepo", "Sincronizado con AWS: ${prodsApi.size} productos reales.")
            }
        } catch (e: Exception) {
            Log.e("MenuRepo", "Error de conexión con AWS: ${e.message}")
        }
    }

    fun getCategoriasActivas(): List<Categoria> = _categorias.value.filter { it.activo }
    fun getProductoById(id: Int): Producto? = _productos.value.find { it.id == id }

    suspend fun obtenerCategoriasPorModulo(modulo: ModuloPedido): List<Categoria> {
        refrescarDesdeApi() 
        val modCat = modulo.toModuloCategoria()
        return _categorias.value.filter { it.activo && (modCat == null || it.modulo == modCat) }
    }

    suspend fun obtenerProductosPorModulo(modulo: ModuloPedido): List<Producto> {
        refrescarDesdeApi() 
        val modCat = modulo.toModuloCategoria()
        return _productos.value.filter { 
            // Mostramos el producto si está disponible en la BD de AWS
            it.disponible && (modCat == null || it.categoria?.modulo == modCat || it.categoria == null)
        }
    }

    suspend fun buscarProductoPorIdEnApi(id: Int): Producto? {
        return try {
            val resp = api.obtenerProducto(id)
            if (resp.isSuccessful) {
                val dto = resp.body()?.data
                val cat = _categorias.value.find { it.id == dto?.categoriaId }
                dto?.toDomain()?.copy(categoria = cat)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun ModuloPedido.toModuloCategoria(): ModuloCategoria? = when (this) {
        ModuloPedido.DESAYUNOS -> ModuloCategoria.DESAYUNOS
        ModuloPedido.COMIDAS   -> ModuloCategoria.COMIDAS
        else -> null
    }

    suspend fun agregarProductoApi(nombre: String, descripcion: String?, precio: BigDecimal, categoriaId: Int, imagenUrl: String? = null) {
        val req = ProductoRequest(idCategoria = categoriaId, nombre = nombre, descripcion = descripcion, precio = precio, disponible = true, imagenUrl = imagenUrl)
        if (api.crearProducto(req).isSuccessful) refrescarDesdeApi()
    }

    suspend fun editarProductoApi(productoId: Int, nombre: String, descripcion: String?, precio: BigDecimal, categoriaId: Int, disponible: Boolean, imagenUrl: String? = null): Boolean {
        val req = ProductoRequest(idCategoria = categoriaId, nombre = nombre, descripcion = descripcion, precio = precio, disponible = disponible, imagenUrl = imagenUrl)
        val exito = api.actualizarProducto(productoId, req).isSuccessful
        if (exito) refrescarDesdeApi()
        return exito
    }

    suspend fun toggleDisponibilidadApi(productoId: Int) {
        val p = getProductoById(productoId) ?: return
        if (api.cambiarDisponibilidadProducto(productoId, !p.disponible).isSuccessful) refrescarDesdeApi()
    }

    suspend fun eliminarProductoApi(productoId: Int) {
        if (api.eliminarProducto(productoId).isSuccessful) refrescarDesdeApi()
    }

    suspend fun recargar() { refrescarDesdeApi() }
}
