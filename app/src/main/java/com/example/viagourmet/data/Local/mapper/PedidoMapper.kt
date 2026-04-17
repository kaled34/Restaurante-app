package com.example.viagourmet.data.Local.mapper

import com.example.viagourmet.data.entity.DetallePedidoEntity
import com.example.viagourmet.data.entity.PedidoConDetalles
import com.example.viagourmet.data.entity.PedidoEntity
import com.example.viagourmet.data.entity.PedidoLibreEntity
import com.example.viagourmet.data.repository.ItemCarrito
import com.example.viagourmet.domain.model.*
import java.math.BigDecimal
import java.time.LocalDateTime

fun Pedido.toEntity(): PedidoEntity = PedidoEntity(
    id = id,
    empleadoId = empleadoId,
    clienteId = clienteId,
    clienteNombre = cliente?.nombre ?: "",
    clienteApellido = cliente?.apellido,
    clienteTelefono = cliente?.telefono,
    clienteEmail = cliente?.email,
    modulo = modulo.name,
    estado = estado.name,
    tipo = tipo.name,
    horarioRecogidaId = horarioRecogidaId,
    notas = notas,
    creadoEn = creadoEn.toString(),
    actualizadoEn = actualizadoEn.toString()
)

fun DetallePedido.toEntity(): DetallePedidoEntity = DetallePedidoEntity(
    id = id,
    pedidoId = pedidoId,
    productoId = productoId,
    productoNombre = producto?.nombre ?: "Producto #$productoId",
    cantidad = cantidad,
    precioUnitario = precioUnitario.toPlainString(),
    notas = notas
)

fun PedidoLibre.toEntity(): PedidoLibreEntity = PedidoLibreEntity(
    id = id,
    pedidoId = pedidoId,
    descripcion = descripcion,
    precioManual = precioManual.toPlainString(),
    cantidad = cantidad,
    adminId = adminId,
    notas = notas,
    creadoEn = creadoEn.toString()
)

fun PedidoConDetalles.toDomain(): Pedido = Pedido(
    id = pedido.id,
    empleadoId = pedido.empleadoId,
    clienteId = pedido.clienteId,
    modulo = ModuloPedido.valueOf(pedido.modulo),
    estado = EstadoPedido.valueOf(pedido.estado),
    tipo = TipoPedido.valueOf(pedido.tipo),
    horarioRecogidaId = pedido.horarioRecogidaId,
    notas = pedido.notas,
    creadoEn = LocalDateTime.parse(pedido.creadoEn),
    actualizadoEn = LocalDateTime.parse(pedido.actualizadoEn),
    detalles = detalles.map { it.toDomain() },
    itemsLibres = itemsLibres.map { it.toDomain() },
    cliente = Cliente(
        id = pedido.clienteId ?: 0,
        nombre = pedido.clienteNombre,
        apellido = pedido.clienteApellido,
        telefono = pedido.clienteTelefono,
        email = pedido.clienteEmail
    ),
    minutosEntrega = null
)

fun DetallePedidoEntity.toDomain(): DetallePedido = DetallePedido(
    id = id,
    pedidoId = pedidoId,
    productoId = productoId,
    cantidad = cantidad,
    precioUnitario = BigDecimal(precioUnitario),
    notas = notas,
    producto = Producto(
        id = productoId,
        categoriaId = 0,
        nombre = productoNombre,
        descripcion = "",
        precio = BigDecimal(precioUnitario),
        disponible = true,
        imagenUrl = null
    )
)

fun PedidoLibreEntity.toDomain(): PedidoLibre = PedidoLibre(
    id = id,
    pedidoId = pedidoId,
    descripcion = descripcion,
    precioManual = BigDecimal(precioManual),
    cantidad = cantidad,
    adminId = adminId,
    notas = notas,
    creadoEn = LocalDateTime.parse(creadoEn)
)

fun ItemCarrito.toDetallePedidoEntity(pedidoId: Int): DetallePedidoEntity = DetallePedidoEntity(
    pedidoId = pedidoId,
    productoId = producto.id,
    productoNombre = producto.nombre,
    cantidad = cantidad,
    precioUnitario = producto.precio.toPlainString(),
    notas = null
)