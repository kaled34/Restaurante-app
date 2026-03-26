package com.example.viagourmet.Presentacion.screens.admin

import androidx.compose.ui.graphics.Color
import com.example.viagourmet.domain.model.EstadoPedido
import com.example.viagourmet.domain.model.ModuloPedido

// EstadoPedido

fun EstadoPedido.displayName(): String = when (this) {
    EstadoPedido.PENDIENTE      -> "Pendiente"
    EstadoPedido.EN_PREPARACION -> "En preparación"
    EstadoPedido.LISTO          -> "Listo"
    EstadoPedido.ENTREGADO      -> "Entregado"
    EstadoPedido.CANCELADO      -> "Cancelado"
}

fun EstadoPedido.siguienteEstado(): EstadoPedido? = when (this) {
    EstadoPedido.PENDIENTE      -> EstadoPedido.EN_PREPARACION
    EstadoPedido.EN_PREPARACION -> EstadoPedido.LISTO
    EstadoPedido.LISTO          -> EstadoPedido.ENTREGADO
    EstadoPedido.ENTREGADO      -> null
    EstadoPedido.CANCELADO      -> null
}

fun EstadoPedido.icono(): String = when (this) {
    EstadoPedido.PENDIENTE      -> "🟡"
    EstadoPedido.EN_PREPARACION -> "🔵"
    EstadoPedido.LISTO          -> "🟢"
    EstadoPedido.ENTREGADO      -> "✅"
    EstadoPedido.CANCELADO      -> "❌"
}

fun EstadoPedido.colorFondo(): Color = when (this) {
    EstadoPedido.PENDIENTE      -> Color(0xFFFFF3E0)
    EstadoPedido.EN_PREPARACION -> Color(0xFFE3F2FD)
    EstadoPedido.LISTO          -> Color(0xFFE8F5E9)
    EstadoPedido.ENTREGADO      -> Color(0xFFF5F5F5)
    EstadoPedido.CANCELADO      -> Color(0xFFFFEBEE)
}

fun EstadoPedido.colorBorde(): Color = when (this) {
    EstadoPedido.PENDIENTE      -> Color(0xFFFF6F00)
    EstadoPedido.EN_PREPARACION -> Color(0xFF1565C0)
    EstadoPedido.LISTO          -> Color(0xFF2E7D32)
    EstadoPedido.ENTREGADO      -> Color(0xFF9E9E9E)
    EstadoPedido.CANCELADO      -> Color(0xFFC62828)
}

fun EstadoPedido.colorTexto(): Color = when (this) {
    EstadoPedido.PENDIENTE      -> Color(0xFFE65100)
    EstadoPedido.EN_PREPARACION -> Color(0xFF0D47A1)
    EstadoPedido.LISTO          -> Color(0xFF1B5E20)
    EstadoPedido.ENTREGADO      -> Color(0xFF616161)
    EstadoPedido.CANCELADO      -> Color(0xFFB71C1C)
}

// ModuloPedido

fun ModuloPedido.displayName(): String = when (this) {
    ModuloPedido.DESAYUNOS -> "Desayunos"
    ModuloPedido.COMIDAS   -> "Comidas"
    ModuloPedido.LIBRE     -> "Libre"
}

fun ModuloPedido.colorFondo(): Color = when (this) {
    ModuloPedido.DESAYUNOS -> Color(0xFFFFF8E1)
    ModuloPedido.COMIDAS   -> Color(0xFFF3E5F5)
    ModuloPedido.LIBRE     -> Color(0xFFE0F2F1)
}

fun ModuloPedido.colorTexto(): Color = when (this) {
    ModuloPedido.DESAYUNOS -> Color(0xFFF57F17)
    ModuloPedido.COMIDAS   -> Color(0xFF6A1B9A)
    ModuloPedido.LIBRE     -> Color(0xFF00695C)
}