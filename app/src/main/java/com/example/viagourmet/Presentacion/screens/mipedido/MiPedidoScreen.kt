package com.example.viagourmet.Presentacion.screens.mipedido

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.viagourmet.domain.model.EstadoPedido
import com.example.viagourmet.domain.model.Pedido

private val Green       = Color(0xFF007E67)
private val GreenDark   = Color(0xFF005C4B)
private val GreenLight  = Color(0xFF00A882)
private val GreenPale   = Color(0xFFE6F4F1)
private val GreenMint   = Color(0xFFF0FAF7)
private val TextDark    = Color(0xFF0D2B24)
private val TextMid     = Color(0xFF4A7A6F)
private val TextLight   = Color(0xFF8AADA7)
private val CardBg      = Color(0xFFFFFFFF)
private val ErrorRed    = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiPedidoScreen(
    viewModel: MiPedidoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = GreenMint,
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Green, GreenDark)))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)).clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                    Column {
                        Text("Estado de mi pedido", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Seguimiento en tiempo real", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green)
                }
                uiState.noPedidoActivo -> {
                    val entregado = uiState.ultimoEntregado
                    if (entregado != null) {
                        PedidoContenido(pedido = entregado, modifier = Modifier.fillMaxSize())
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Text("🍽", fontSize = 56.sp)
                                Text("Sin pedidos activos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                                Text("Haz un pedido desde el menú para verlo aquí", fontSize = 14.sp, color = TextLight)
                                Spacer(Modifier.height(4.dp))
                                Button(onClick = onNavigateBack, shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                                    Text("Ir al menú", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                else -> PedidoContenido(pedido = uiState.pedidoActivo!!, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
private fun PedidoContenido(pedido: Pedido, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header del pedido
        Card(
            modifier = Modifier.fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Green.copy(alpha = 0.15f)),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Green, GreenLight)))
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Pedido #${pedido.id}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        pedido.notas?.let { Text(it, fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f)) }
                    }
                    Text(pedido.estado.icono(), fontSize = 36.sp)
                }
            }
        }

        // Timeline de estados
        Text("Estado del pedido", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)

        Card(
            modifier = Modifier.fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = Green.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                val estados = listOf(
                    EstadoPedido.PENDIENTE      to "Pedido recibido",
                    EstadoPedido.EN_PREPARACION to "En preparación",
                    EstadoPedido.LISTO          to "Listo para recoger"
                )
                val indexActual = estados.indexOfFirst { it.first == pedido.estado }

                estados.forEachIndexed { index, (estado, label) ->
                    val isPasado  = index <= indexActual
                    val isActual  = pedido.estado == estado

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Indicador
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(24.dp).clip(CircleShape)
                                    .background(when {
                                        isActual -> Green
                                        isPasado -> GreenLight
                                        else     -> Color(0xFFE0E0E0)
                                    }),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (isPasado && !isActual) "✓" else if (isActual) "●" else "○",
                                    fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold
                                )
                            }
                            if (index < estados.lastIndex) {
                                Box(
                                    modifier = Modifier.width(2.dp).height(24.dp)
                                        .background(if (index < indexActual) GreenLight else Color(0xFFE0E0E0))
                                )
                            }
                        }

                        Column {
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = if (isActual) FontWeight.Bold else FontWeight.Normal,
                                color = if (isActual) Green else if (isPasado) TextMid else TextLight
                            )
                            if (isActual) {
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(GreenPale).padding(horizontal = 8.dp, vertical = 2.dp)
                                ) { Text("Actual", fontSize = 11.sp, color = Green, fontWeight = FontWeight.SemiBold) }
                            }
                        }
                    }
                }
            }
        }

        // Banner especial
        when (pedido.estado) {
            EstadoPedido.LISTO -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenPale)
                ) {
                    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🎉", fontSize = 24.sp)
                        Text("¡Tu pedido está listo! Pasa a recogerlo.", fontSize = 14.sp, color = Green, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            EstadoPedido.CANCELADO -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.08f))
                ) {
                    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("❌", fontSize = 22.sp)
                        Text("Tu pedido ha sido cancelado.", fontSize = 14.sp, color = ErrorRed, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            EstadoPedido.ENTREGADO -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("✅", fontSize = 22.sp)
                        Text("Pedido entregado. ¡Buen provecho!", fontSize = 14.sp, color = TextMid, fontWeight = FontWeight.Medium)
                    }
                }
            }
            else -> {}
        }

        // Resumen de productos
        if (pedido.detalles.isNotEmpty()) {
            Text("Resumen", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pedido.detalles.forEach { detalle ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${detalle.cantidad}× ${detalle.producto?.nombre ?: "Producto"}", fontSize = 14.sp, color = TextDark)
                            Text("$${"%.2f".format(detalle.subtotal)}", fontSize = 14.sp, color = Green, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFCCE8E2)))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                        Text("$${"%.2f".format(pedido.detalles.sumOf { it.subtotal })}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Green)
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

private fun EstadoPedido.icono(): String = when (this) {
    EstadoPedido.PENDIENTE      -> "🟡"
    EstadoPedido.EN_PREPARACION -> "🔵"
    EstadoPedido.LISTO          -> "🟢"
    EstadoPedido.ENTREGADO      -> "✅"
    EstadoPedido.CANCELADO      -> "❌"
}