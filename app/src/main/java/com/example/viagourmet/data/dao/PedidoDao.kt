package com.example.viagourmet.data.local.dao

import androidx.room.*
import com.example.viagourmet.data.entity.PedidoEntity
import com.example.viagourmet.data.local.entity.DetallePedidoEntity
import com.example.viagourmet.data.local.entity.PedidoConDetalles
import com.example.viagourmet.data.local.entity.PedidoLibreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {



    @Transaction
    @Query("SELECT * FROM pedidos ORDER BY id DESC")
    fun getAllPedidosFlow(): Flow<List<PedidoConDetalles>>


    @Transaction
    @Query("SELECT * FROM pedidos WHERE estado NOT IN ('ENTREGADO','CANCELADO') ORDER BY id DESC")
    fun getPedidosActivosFlow(): Flow<List<PedidoConDetalles>>

    @Transaction
    @Query("SELECT * FROM pedidos WHERE clienteId = :clienteId ORDER BY id DESC")
    fun getPedidosByClienteFlow(clienteId: Int): Flow<List<PedidoConDetalles>>

    @Transaction
    @Query("SELECT * FROM pedidos WHERE id = :pedidoId")
    suspend fun getPedidoById(pedidoId: Int): PedidoConDetalles?

    //inserciones

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPedido(pedido: PedidoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetalles(detalles: List<DetallePedidoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItemsLibres(items: List<PedidoLibreEntity>)

//actualizaciones
    @Query("UPDATE pedidos SET estado = :estado, actualizadoEn = :ahora WHERE id = :pedidoId")
    suspend fun actualizarEstado(pedidoId: Int, estado: String, ahora: String): Int

//eliminaciones
    @Query("DELETE FROM pedidos")
    suspend fun deleteAll()
}