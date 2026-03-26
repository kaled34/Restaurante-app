package com.example.viagourmet.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.viagourmet.data.entity.PedidoEntity

@Entity(
    tableName = "pedido_libre",
    foreignKeys = [
        ForeignKey(
            entity = PedidoEntity::class,
            parentColumns = ["id"],
            childColumns = ["pedidoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("pedidoId")]
)
data class PedidoLibreEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val pedidoId: Int,
    val descripcion: String,
    val precioManual: String,
    val cantidad: Int,
    val adminId: Int,
    val notas: String?,
    val creadoEn: String)