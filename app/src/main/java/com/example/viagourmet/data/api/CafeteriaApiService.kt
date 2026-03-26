package com.example.viagourmet.data.api

import com.example.viagourmet.data.model.PedidoDto
import com.example.viagourmet.data.model.request.CrearPedidoLibreRequest
import com.example.viagourmet.data.model.request.FcmTokenRequest
import com.example.viagourmet.data.model.Response.ApiResponse
import retrofit2.Response
import retrofit2.http.*

interface CafeteriaApiService {

    // ── Pedidos ──────────────────────────────────────────────────────────────

    @GET("api/v1/pedidos")
    suspend fun listarPedidos(): Response<ApiResponse<List<PedidoDto>>>

    @GET("api/v1/pedidos")
    suspend fun listarPedidosPorEstado(
        @Query("estado") estado: String
    ): Response<ApiResponse<List<PedidoDto>>>

    @GET("api/v1/pedidos/cliente/{id}")
    suspend fun listarPedidosPorCliente(
        @Path("id") clienteId: Int
    ): Response<ApiResponse<List<PedidoDto>>>

    @PATCH("api/v1/pedidos/{id}/estado")
    suspend fun cambiarEstadoPedido(
        @Path("id") idPedido: Int,
        @Query("estado") estado: String
    ): Response<ApiResponse<PedidoDto>>

    @POST("api/v1/pedidos-libres")
    suspend fun crearPedidoLibre(
        @Body request: CrearPedidoLibreRequest
    ): Response<ApiResponse<Any>>

    // ── Clientes ─────────────────────────────────────────────────────────────

    @PATCH("api/v1/clientes/{id}/fcm-token")
    suspend fun actualizarFcmToken(
        @Path("id") clienteId: Int,
        @Body request: FcmTokenRequest
    ): Response<ApiResponse<Any>>
}
