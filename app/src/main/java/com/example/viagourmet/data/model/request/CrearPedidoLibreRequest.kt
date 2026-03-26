package com.example.viagourmet.data.model.request

import com.google.gson.annotations.SerializedName

data class CrearPedidoLibreRequest(
    @SerializedName("id_cliente")      val idCliente: Int?,
    @SerializedName("notas")           val notas: String,
    @SerializedName("modulo")          val modulo: String,
    @SerializedName("tipo")            val tipo: String,
    @SerializedName("minutos_entrega") val minutosEntrega: Int,
    @SerializedName("metodo_pago")     val metodoPago: String,
    @SerializedName("id_empleado")     val idEmpleado: Int = 1  // empleado default para pedidos de cliente
)
