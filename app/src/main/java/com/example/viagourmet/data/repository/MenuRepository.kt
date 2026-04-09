package com.example.viagourmet.data.repository

import com.example.viagourmet.data.api.CafeteriaApiService
import com.example.viagourmet.domain.model.Categoria
import com.example.viagourmet.domain.model.ModuloPedido
import com.example.viagourmet.domain.model.Producto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MenuRepositoryImpl — obtiene categorías y productos desde la API.
 *
 * El MenuViewModel actualmente usa MockData. Con este repositorio
 * se puede migrar a datos reales del servidor en una sola línea de cambio.
 *
 * Mapeo de módulo app → API:
 *   DESAYUNOS  →  "desayunos"
 *   COMIDAS    →  "comidas"
 *   LIBRE      →  "libre"
 */
@Singleton
class MenuRepositoryImpl @Inject constructor(
    private val api: CafeteriaApiService
) {
    /** Obtiene las categorías activas del módulo especificado. */
    suspend fun obtenerCategoriasPorModulo(modulo: ModuloPedido): List<Categoria> {
        return try {
            val moduloStr = when (modulo) {
                ModuloPedido.DESAYUNOS -> "desayunos"
                ModuloPedido.COMIDAS   -> "comidas"
                ModuloPedido.LIBRE     -> "libre"
            }
            val resp = api.listarCategoriasPorModulo(moduloStr)
            if (!resp.isSuccessful) return emptyList()
            resp.body()?.data?.map { it.toDomain() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Obtiene los productos disponibles del módulo especificado. */
    suspend fun obtenerProductosPorModulo(modulo: ModuloPedido): List<Producto> {
        return try {
            // Obtener las categorías del módulo primero
            val categorias = obtenerCategoriasPorModulo(modulo)
            if (categorias.isEmpty()) return emptyList()

            // Traer todos los productos disponibles y filtrar por categoría del módulo
            val resp = api.listarProductos(disponible = true)
            if (!resp.isSuccessful) return emptyList()

            val idsCategorias = categorias.map { it.id }.toSet()
            resp.body()?.data
                ?.filter { it.categoriaId in idsCategorias }
                ?.map { dto ->
                    // Inyectar la categoría en el producto
                    val categoria = categorias.find { it.id == dto.categoriaId }
                    dto.toDomain().copy(categoria = categoria)
                }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Obtiene todos los productos de una categoría específica. */
    suspend fun obtenerProductosPorCategoria(categoriaId: Int): List<Producto> {
        return try {
            val resp = api.listarProductosPorCategoria(categoriaId)
            if (!resp.isSuccessful) return emptyList()
            resp.body()?.data?.map { it.toDomain() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}