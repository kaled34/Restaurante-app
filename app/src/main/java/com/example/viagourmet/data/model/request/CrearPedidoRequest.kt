package com.example.viagourmet.data.model.request

import com.google.gson.annotations.SerializedName

class CrearPedidoRequest {
    data class CrearPedidoRequest(
        @SerializedName("id_empleado")
        val empleadoId: Int,

        @SerializedName("id_cliente")
        val clienteId: Int?,

        @SerializedName("modulo")
        val modulo: String,

        @SerializedName("tipo")
        val tipo: String,

        @SerializedName("id_horario_recogida")
        val horarioRecogidaId: Int?,

        @SerializedName("notas")
        val notas: String?,

        @SerializedName("detalles")
        val detalles: List<DetallePedidoRequest>
    )

    data class DetallePedidoRequest(
        @SerializedName("id_producto")
        val productoId: Int,

        @SerializedName("cantidad")
        val cantidad: Int,

        @SerializedName("notas")
        val notas: String?
    )
}