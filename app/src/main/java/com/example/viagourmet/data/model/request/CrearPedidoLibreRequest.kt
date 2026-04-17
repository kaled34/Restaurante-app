package com.example.viagourmet.data.model.request

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class CrearPedidoLibreItemRequest(
    @SerializedName("id_pedido")      val idPedido: Int,
    @SerializedName("descripcion")    val descripcion: String,
    @SerializedName("precio_manual")  val precioManual: BigDecimal,
    @SerializedName("cantidad")       val cantidad: Int = 1,
    @SerializedName("id_admin")       val idAdmin: Int,
    @SerializedName("notas")          val notas: String? = null
)