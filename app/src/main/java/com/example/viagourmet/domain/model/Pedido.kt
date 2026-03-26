package com.example.viagourmet.domain.model

import java.time.LocalDateTime

data class Pedido(
    val id: Int,
    val empleadoId: Int,
    val clienteId: Int?,
    val modulo: ModuloPedido,
    val estado: EstadoPedido,
    val tipo: TipoPedido,
    val horarioRecogidaId: Int?,
    val notas: String?,
    val creadoEn: LocalDateTime,
    val actualizadoEn: LocalDateTime,
    val detalles: List<DetallePedido> = emptyList(),
    val itemsLibres: List<PedidoLibre> = emptyList(),
    val empleado: Empleado? = null,
    val cliente: Cliente? = null,
    val factura: Factura? = null
)

enum class ModuloPedido {
    DESAYUNOS,
    COMIDAS,
    LIBRE;

    companion object {
        fun fromString(value: String): ModuloPedido {
            return when (value.lowercase()) {
                "desayunos" -> DESAYUNOS
                "comidas" -> COMIDAS
                "libre" -> LIBRE
                else -> COMIDAS
            }
        }
    }
}

enum class EstadoPedido {
    PENDIENTE,
    EN_PREPARACION,
    LISTO,
    ENTREGADO,
    CANCELADO;

    companion object {
        fun fromString(value: String): EstadoPedido {
            return when (value.lowercase()) {
                "pendiente" -> PENDIENTE
                "en_preparacion" -> EN_PREPARACION
                "listo" -> LISTO
                "entregado" -> ENTREGADO
                "cancelado" -> CANCELADO
                else -> PENDIENTE
            }
        }
    }
}

enum class TipoPedido {
    PARA_LLEVAR,
    OFICINA;

    companion object {
        fun fromString(value: String): TipoPedido {
            return when (value.lowercase()) {
                "para_llevar" -> PARA_LLEVAR
                "oficina" -> OFICINA
                else -> PARA_LLEVAR
            }
        }
    }
}