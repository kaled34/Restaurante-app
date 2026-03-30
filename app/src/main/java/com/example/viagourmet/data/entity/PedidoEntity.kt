package com.example.viagourmet.data.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

// CORRECCIÓN: PedidoEntity se mueve de "data.entity" a "data.local.entity"
// para ser consistente con DetallePedidoEntity, PedidoLibreEntity y UsuarioEntity.
// Esto resuelve el error "Cannot resolve symbol 'pedidos'" en PedidoDao porque
// Room KSP necesita que todas las entidades estén en el mismo package coherente
// y que las ForeignKey de DetallePedidoEntity/PedidoLibreEntity puedan resolverla.

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