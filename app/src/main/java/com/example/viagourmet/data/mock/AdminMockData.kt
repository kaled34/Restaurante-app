package com.example.viagourmet.data.mock

import com.example.viagourmet.domain.model.*
import java.math.BigDecimal
import java.time.LocalDateTime

object AdminMockData {

    val pedidos = mutableListOf(
        Pedido(
            id = 1,
            empleadoId = 1,
            clienteId = 10,
            modulo = ModuloPedido.DESAYUNOS,
            estado = EstadoPedido.PENDIENTE,
            tipo = TipoPedido.PARA_LLEVAR,
            horarioRecogidaId = null,
            notas = "Sin azúcar en el café",
            creadoEn = LocalDateTime.now().minusMinutes(5),
            actualizadoEn = LocalDateTime.now().minusMinutes(5),
            detalles = listOf(
                DetallePedido(
                    id = 1, pedidoId = 1, productoId = 1, cantidad = 2,
                    precioUnitario = BigDecimal("35.00"), notas = null,
                    producto = MockData.productos.find { it.id == 1 }
                ),
                DetallePedido(
                    id = 2, pedidoId = 1, productoId = 3, cantidad = 1,
                    precioUnitario = BigDecimal("95.00"), notas = "Extra crema",
                    producto = MockData.productos.find { it.id == 3 }
                )
            ),
            itemsLibres = emptyList(),
            cliente = Cliente(
                id = 10, nombre = "Ana", apellido = "García",
                telefono = "9611234567", email = "ana@email.com"
            )
        ),
        Pedido(
            id = 2,
            empleadoId = 1,
            clienteId = 11,
            modulo = ModuloPedido.COMIDAS,
            estado = EstadoPedido.EN_PREPARACION,
            tipo = TipoPedido.OFICINA,
            horarioRecogidaId = null,
            notas = null,
            creadoEn = LocalDateTime.now().minusMinutes(15),
            actualizadoEn = LocalDateTime.now().minusMinutes(10),
            detalles = listOf(
                DetallePedido(
                    id = 3, pedidoId = 2, productoId = 4, cantidad = 1,
                    precioUnitario = BigDecimal("120.00"), notas = null,
                    producto = MockData.productos.find { it.id == 4 }
                ),
                DetallePedido(
                    id = 4, pedidoId = 2, productoId = 6, cantidad = 2,
                    precioUnitario = BigDecimal("40.00"), notas = null,
                    producto = MockData.productos.find { it.id == 6 }
                )
            ),
            itemsLibres = emptyList(),
            cliente = Cliente(
                id = 11, nombre = "Carlos", apellido = "Ruiz",
                telefono = "9619876543", email = "carlos@email.com"
            )
        ),
        Pedido(
            id = 3,
            empleadoId = 1,
            clienteId = 12,
            modulo = ModuloPedido.DESAYUNOS,
            estado = EstadoPedido.LISTO,
            tipo = TipoPedido.PARA_LLEVAR,
            horarioRecogidaId = null,
            notas = "Alergia al gluten — revisar",
            creadoEn = LocalDateTime.now().minusMinutes(30),
            actualizadoEn = LocalDateTime.now().minusMinutes(5),
            detalles = listOf(
                DetallePedido(
                    id = 5, pedidoId = 3, productoId = 2, cantidad = 3,
                    precioUnitario = BigDecimal("85.00"), notas = null,
                    producto = MockData.productos.find { it.id == 2 }
                )
            ),
            itemsLibres = emptyList(),
            cliente = Cliente(
                id = 12, nombre = "María", apellido = "López",
                telefono = null, email = "maria@email.com"
            )
        ),
        Pedido(
            id = 4,
            empleadoId = 1,
            clienteId = 13,
            modulo = ModuloPedido.COMIDAS,
            estado = EstadoPedido.PENDIENTE,
            tipo = TipoPedido.PARA_LLEVAR,
            horarioRecogidaId = null,
            notas = null,
            creadoEn = LocalDateTime.now().minusMinutes(2),
            actualizadoEn = LocalDateTime.now().minusMinutes(2),
            detalles = listOf(
                DetallePedido(
                    id = 6, pedidoId = 4, productoId = 5, cantidad = 2,
                    precioUnitario = BigDecimal("135.00"), notas = "Sin arroz",
                    producto = MockData.productos.find { it.id == 5 }
                )
            ),
            itemsLibres = emptyList(),
            cliente = Cliente(
                id = 13, nombre = "Luis", apellido = "Hernández",
                telefono = "9615554433", email = null
            )
        ),
        Pedido(
            id = 5,
            empleadoId = 1,
            clienteId = 14,
            modulo = ModuloPedido.LIBRE,
            estado = EstadoPedido.ENTREGADO,
            tipo = TipoPedido.OFICINA,
            horarioRecogidaId = null,
            notas = "Pedido especial de gerencia",
            creadoEn = LocalDateTime.now().minusHours(1),
            actualizadoEn = LocalDateTime.now().minusMinutes(20),
            detalles = emptyList(),
            itemsLibres = listOf(
                PedidoLibre(
                    id = 1, pedidoId = 5, descripcion = "Platillo del día especial",
                    precioManual = BigDecimal("180.00"), cantidad = 2,
                    adminId = 1, notas = "Preparación especial del chef",
                    creadoEn = LocalDateTime.now().minusHours(1)
                )
            ),
            cliente = Cliente(
                id = 14, nombre = "Patricia", apellido = "Morales",
                telefono = "9612223344", email = "patricia@empresa.com"
            )
        )
    )
}