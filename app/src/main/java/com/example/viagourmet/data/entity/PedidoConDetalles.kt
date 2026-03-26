package com.example.viagourmet.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.example.viagourmet.data.entity.PedidoEntity


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