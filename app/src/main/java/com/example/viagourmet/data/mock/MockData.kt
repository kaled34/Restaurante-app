package com.example.viagourmet.data.mock

import com.example.viagourmet.domain.model.*
import java.math.BigDecimal

object MockData {

    val categorias = listOf(
        Categoria(id = 1, nombre = "Café y Bebidas", descripcion = "Bebidas calientes y frías",
            modulo = ModuloCategoria.DESAYUNOS, activo = true),
        Categoria(id = 2, nombre = "Desayunos", descripcion = "Comienza tu día con energía",
            modulo = ModuloCategoria.DESAYUNOS, activo = true),
        Categoria(id = 3, nombre = "Comidas", descripcion = "Platillos fuertes",
            modulo = ModuloCategoria.COMIDAS, activo = true),
        Categoria(id = 4, nombre = "Guarniciones", descripcion = "Complementos para tu comida",
            modulo = ModuloCategoria.COMIDAS, activo = true)
    )

    val productos = listOf(
        // ── Desayunos ────────────────────────────────────────────────────────
        Producto(id = 1, categoriaId = 1, nombre = "Café Americano",
            descripcion = "Café recién hecho, aroma intenso", precio = BigDecimal("35.00"),
            disponible = true, imagenUrl = null, categoria = categorias[0]),
        Producto(id = 2, categoriaId = 1, nombre = "Café Latte",
            descripcion = "Espresso con leche vaporizada", precio = BigDecimal("45.00"),
            disponible = true, imagenUrl = null, categoria = categorias[0]),
        Producto(id = 3, categoriaId = 1, nombre = "Jugo de Naranja",
            descripcion = "Naranja natural, recién exprimido", precio = BigDecimal("40.00"),
            disponible = true, imagenUrl = null, categoria = categorias[0]),
        Producto(id = 4, categoriaId = 2, nombre = "Hot Cakes",
            descripcion = "Tres hot cakes con miel y mantequilla", precio = BigDecimal("85.00"),
            disponible = true, imagenUrl = null, categoria = categorias[1]),
        Producto(id = 5, categoriaId = 2, nombre = "Chilaquiles",
            descripcion = "Con pollo, crema y queso fresco", precio = BigDecimal("95.00"),
            disponible = true, imagenUrl = null, categoria = categorias[1]),
        Producto(id = 6, categoriaId = 2, nombre = "Enfrijoladas",
            descripcion = "Con crema, queso y cebolla", precio = BigDecimal("90.00"),
            disponible = true, imagenUrl = null, categoria = categorias[1]),
        Producto(id = 7, categoriaId = 2, nombre = "Huevos a la mexicana",
            descripcion = "Con jitomate, cebolla y chile serrano", precio = BigDecimal("80.00"),
            disponible = true, imagenUrl = null, categoria = categorias[1]),
        // ── Comidas ──────────────────────────────────────────────────────────
        Producto(id = 8, categoriaId = 3, nombre = "Hamburguesa Clásica",
            descripcion = "120g de carne, queso, lechuga y tomate", precio = BigDecimal("120.00"),
            disponible = true, imagenUrl = null, categoria = categorias[2]),
        Producto(id = 9, categoriaId = 3, nombre = "Pechuga a la plancha",
            descripcion = "Con verduras salteadas y arroz", precio = BigDecimal("135.00"),
            disponible = true, imagenUrl = null, categoria = categorias[2]),
        Producto(id = 10, categoriaId = 3, nombre = "Pasta a la boloñesa",
            descripcion = "Pasta con salsa de carne y jitomate", precio = BigDecimal("110.00"),
            disponible = true, imagenUrl = null, categoria = categorias[2]),
        Producto(id = 11, categoriaId = 3, nombre = "Menú del día",
            descripcion = "Sopa, guisado, agua y postre", precio = BigDecimal("95.00"),
            disponible = true, imagenUrl = null, categoria = categorias[2]),
        Producto(id = 12, categoriaId = 4, nombre = "Arroz",
            descripcion = "Arroz rojo estilo mexicano", precio = BigDecimal("30.00"),
            disponible = true, imagenUrl = null, categoria = categorias[3]),
        Producto(id = 13, categoriaId = 4, nombre = "Frijoles de la olla",
            descripcion = "Frijoles negros con epazote", precio = BigDecimal("30.00"),
            disponible = true, imagenUrl = null, categoria = categorias[3])
    )

    fun getCategoriasActivas(): List<Categoria> = categorias.filter { it.activo }

    fun getProductosByCategoria(categoriaId: Int): List<Producto> =
        productos.filter { it.categoriaId == categoriaId && it.disponible }

    fun getProductosPorModulo(modulo: ModuloPedido): List<Producto> {
        val moduloCategoria = when (modulo) {
            ModuloPedido.DESAYUNOS -> ModuloCategoria.DESAYUNOS
            ModuloPedido.COMIDAS   -> ModuloCategoria.COMIDAS
            ModuloPedido.LIBRE     -> null
        }
        return if (moduloCategoria == null) productos
        else productos.filter { it.categoria?.modulo == moduloCategoria && it.disponible }
    }
}
