package com.example.viagourmet.Presentacion.screens.menu

import androidx.lifecycle.ViewModel
import com.example.viagourmet.data.repository.MenuRepository
import com.example.viagourmet.data.repository.PedidoRepositoryLocal
import com.example.viagourmet.domain.model.Producto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProductoDetalleViewModel @Inject constructor(
    private val repository: PedidoRepositoryLocal,
    private val menuRepository: MenuRepository
) : ViewModel() {

    /** Busca el producto en el repositorio reactivo (no en MockData). */
    fun getProductoById(id: Int): Producto? = menuRepository.getProductoById(id)

    fun agregarAlCarrito(producto: Producto, cantidad: Int) {
        repository.agregarAlCarrito(producto, cantidad)
    }
}