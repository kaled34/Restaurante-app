package com.example.viagourmet.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class PedidoConDetalles(
    @Embedded val pedido: PedidoEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "pedidoId"
    )
    val detalles: List<DetallePedidoEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "pedidoId"
    )
    val itemsLibres: List<PedidoLibreEntity>
)