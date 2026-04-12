package com.example.viagourmet.data.api

import com.example.viagourmet.data.model.*
import com.example.viagourmet.data.model.Response.ApiResponse
import com.example.viagourmet.data.model.Response.AuthResponse
import com.example.viagourmet.data.model.request.*
import retrofit2.Response
import retrofit2.http.*

interface CafeteriaApiService {

    // ── Clientes ─────────────────────────────────────────────────────────────
    @GET("api/v1/clientes/email/{email}")
    suspend fun buscarClientePorEmail(
        @Path("email") email: String
    ): Response<ApiResponse<ClienteDto>>

    @POST("api/v1/clientes")
    suspend fun crearCliente(
        @Body request: RegistroClienteRequest.RegistroClienteRequest
    ): Response<ApiResponse<ClienteDto>>

    @GET("api/v1/clientes/{id}")
    suspend fun obtenerCliente(
        @Path("id") id: Int
    ): Response<ApiResponse<ClienteDto>>

    @PUT("api/v1/clientes/{id}")
    suspend fun actualizarCliente(
        @Path("id") id: Int,
        @Body request: RegistroClienteRequest.RegistroClienteRequest
    ): Response<ApiResponse<ClienteDto>>

    // ── Empleados ─────────────────────────────────────────────────────────────
    @GET("api/v1/empleados")
    suspend fun listarEmpleados(): Response<ApiResponse<List<EmpleadoDto>>>

    @GET("api/v1/empleados/{id}")
    suspend fun obtenerEmpleado(
        @Path("id") id: Int
    ): Response<ApiResponse<EmpleadoDto>>

    @POST("api/v1/empleados/login")
    suspend fun loginEmpleado(
        @Body request: LoginRequest.LoginRequest
    ): Response<ApiResponse<LoginEmpleadoResponse>>

    // ── Categorías ────────────────────────────────────────────────────────────
    @GET("api/v1/categorias/activas")
    suspend fun listarCategoriasActivas(): Response<ApiResponse<List<CategoriaDto>>>

    @GET("api/v1/categorias/modulo/{modulo}")
    suspend fun listarCategoriasPorModulo(
        @Path("modulo") modulo: String
    ): Response<ApiResponse<List<CategoriaDto>>>

    // ── Productos ─────────────────────────────────────────────────────────────
    @GET("api/v1/productos")
    suspend fun listarProductos(
        @Query("categoriaId") categoriaId: Int? = null,
        @Query("disponible") disponible: Boolean? = null
    ): Response<ApiResponse<List<ProductoDto>>>

    @GET("api/v1/productos/{id}")
    suspend fun obtenerProducto(
        @Path("id") id: Int
    ): Response<ApiResponse<ProductoDto>>

    @GET("api/v1/productos/categoria/{idCategoria}")
    suspend fun listarProductosPorCategoria(
        @Path("idCategoria") idCategoria: Int
    ): Response<ApiResponse<List<ProductoDto>>>

    @POST("api/v1/productos")
    suspend fun crearProducto(
        @Body request: ProductoRequest
    ): Response<ApiResponse<ProductoDto>>

    @PUT("api/v1/productos/{id}")
    suspend fun actualizarProducto(
        @Path("id") id: Int,
        @Body request: ProductoRequest
    ): Response<ApiResponse<ProductoDto>>

    @PATCH("api/v1/productos/{id}/disponibilidad")
    suspend fun cambiarDisponibilidadProducto(
        @Path("id") id: Int,
        @Query("disponible") disponible: Boolean
    ): Response<ApiResponse<ProductoDto>>

    @DELETE("api/v1/productos/{id}")
    suspend fun eliminarProducto(
        @Path("id") id: Int
    ): Response<ApiResponse<Void>>

    // ── Pedidos ───────────────────────────────────────────────────────────────
    @GET("api/v1/pedidos")
    suspend fun listarPedidos(
        @Query("estado") estado: String? = null,
        @Query("modulo") modulo: String? = null
    ): Response<ApiResponse<List<PedidoDto>>>

    @GET("api/v1/pedidos/{id}")
    suspend fun obtenerPedido(
        @Path("id") id: Int
    ): Response<ApiResponse<PedidoDto>>

    @GET("api/v1/pedidos/cliente/{idCliente}")
    suspend fun listarPedidosPorCliente(
        @Path("idCliente") clienteId: Int
    ): Response<ApiResponse<List<PedidoDto>>>

    @GET("api/v1/pedidos/empleado/{idEmpleado}")
    suspend fun listarPedidosPorEmpleado(
        @Path("idEmpleado") empleadoId: Int
    ): Response<ApiResponse<List<PedidoDto>>>

    @POST("api/v1/pedidos")
    suspend fun crearPedido(
        @Body request: CrearPedidoRequest.CrearPedidoRequest
    ): Response<ApiResponse<PedidoDto>>

    @PATCH("api/v1/pedidos/{id}/estado")
    suspend fun cambiarEstadoPedido(
        @Path("id") idPedido: Int,
        @Query("estado") estado: String
    ): Response<ApiResponse<PedidoDto>>

    @DELETE("api/v1/pedidos/{id}")
    suspend fun cancelarPedido(
        @Path("id") idPedido: Int
    ): Response<ApiResponse<Void>>

    // ── Pedidos libres ────────────────────────────────────────────────────────
    @POST("api/v1/pedidos-libres")
    suspend fun crearItemPedidoLibre(
        @Body request: CrearPedidoLibreItemRequest
    ): Response<ApiResponse<Any>>

    @GET("api/v1/pedidos-libres/pedido/{idPedido}")
    suspend fun listarItemsLibresPorPedido(
        @Path("idPedido") idPedido: Int
    ): Response<ApiResponse<List<PedidoLibreDto>>>

    // ── Detalles de pedido ────────────────────────────────────────────────────
    @GET("api/v1/detalles/pedido/{idPedido}")
    suspend fun listarDetallesPorPedido(
        @Path("idPedido") idPedido: Int
    ): Response<ApiResponse<List<DetallePedidoDto>>>

    // ── Horarios disponibles ──────────────────────────────────────────────────
    @GET("api/v1/horarios")
    suspend fun listarHorarios(
        @Query("activo") activo: Boolean? = null
    ): Response<ApiResponse<List<HorarioDisponibleDto>>>

    // ── Facturas ──────────────────────────────────────────────────────────────
    @GET("api/v1/facturas/pedido/{idPedido}")
    suspend fun obtenerFacturaPorPedido(
        @Path("idPedido") idPedido: Int
    ): Response<ApiResponse<FacturaDto>>

    // ── FCM Token ─────────────────────────────────────────────────────────────
    @PATCH("api/v1/clientes/{id}/fcm-token")
    suspend fun actualizarFcmToken(
        @Path("id") clienteId: Int,
        @Body request: FcmTokenRequest
    ): Response<ApiResponse<Any>>
}