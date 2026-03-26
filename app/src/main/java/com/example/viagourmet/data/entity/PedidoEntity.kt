package com.example.viagourmet.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pedidos")
data class PedidoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val empleadoId: Int,
    val clienteId: Int?,
    val clienteNombre: String,
    val clienteApellido: String?,
    val clienteTelefono: String?,
    val clienteEmail: String?,
    val modulo: String,
    val estado: String,
    val tipo: String,
    val horarioRecogidaId: Int?,
    val notas: String?,
    val creadoEn: String,
    val actualizadoEn: String
)