package com.example.viagourmet.Presentacion.screens.menu

import androidx.lifecycle.ViewModel
import com.example.viagourmet.data.repository.PedidoRepositoryLocal
import com.example.viagourmet.domain.model.Producto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ProductoDetalleViewModel @Inject constructor(
    private val repository: PedidoRepositoryLocal
) : ViewModel() {

    fun agregarAlCarrito(producto: Producto, cantidad: Int) {
        repository.agregarAlCarrito(producto, cantidad)
    }
}