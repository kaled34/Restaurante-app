package com.example.viagourmet.domain.model

data class ItemCarrito(val id: Int, val producto: Producto, val cantidad: Int)
data class ItemPedido(val producto: Producto, val cantidad: Int)
