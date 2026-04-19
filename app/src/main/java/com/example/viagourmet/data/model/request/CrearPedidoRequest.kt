package com.example.viagourmet.data.model.request

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

class CrearPedidoRequest {
    data class CrearPedidoRequest(
        @SerializedName("idEmpleado")
        val empleadoId: Int,

        @SerializedName("idCliente")
        val clienteId: Int?,

        @SerializedName("modulo")
        val modulo: String,

        @SerializedName("notas")
        val notas: String?,

        @SerializedName("tipo")
        val tipo: String = "para_llevar",

        @SerializedName("horarioRecogidaId")
        val horarioRecogidaId: Int? = null,

        @SerializedName("detalles")
        val detalles: List<DetallePedidoRequest>
    )

    data class DetallePedidoRequest(
        @SerializedName("idProducto")
        val productoId: Int,

        @SerializedName("cantidad")
        val cantidad: Int,

        @SerializedName("precioUnitario")
        val precioUnitario: BigDecimal,
        
        @SerializedName("notas")
        val notas: String? = null
    )
}
