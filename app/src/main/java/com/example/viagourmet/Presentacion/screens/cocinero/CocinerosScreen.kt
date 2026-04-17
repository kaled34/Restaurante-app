package com.example.viagourmet.Presentacion.screens.cocinero

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ExitToApp
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
import java.time.format.DateTimeFormatter

private val Green       = Color(0xFF007E67)
private val GreenDark   = Color(0xFF005C4B)
private val GreenMint   = Color(0xFFF0FAF7)
private val TextDark    = Color(0xFF0D2B24)
private val TextMid     = Color(0xFF4A7A6F)
private val TextLight   = Color(0xFF8AADA7)
private val CardBg      = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocinerosScreen(
    viewModel: CocinerosViewModel = hiltViewModel(),
    onCerrarSesion: () -> Unit
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("🍳 Cocina", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Pedidos activos · CONALEP", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                    Box(
                        modifier = Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)).clickable { onCerrarSesion() },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.ExitToApp, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // ── Leyenda urgencia ────────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(GreenDark, Green)))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    UrgenciaTag(Color(0xFFE53935), "🔴 ≤35 min")
                    UrgenciaTag(Color(0xFFFDD835), "🟡 45 min")
                    UrgenciaTag(Color(0xFF43A047), "🟢 60+ min")
                }
            }

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green)
                }
                uiState.pedidos.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("✅", fontSize = 48.sp)
                        Text("Sin pedidos activos", fontSize = 16.sp, color = TextLight)
                        Text("¡Todo al corriente!", fontSize = 13.sp, color = TextLight)
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.pedidos, key = { it.id }) { pedido ->
                        CocineroCard(pedido = pedido, onCambiarEstado = { viewModel.cambiarEstado(pedido.id, it) })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }

            uiState.errorMessage?.let {
                Snackbar(modifier = Modifier.padding(16.dp)) { Text(it) }
            }
        }
    }
}

@Composable
private fun UrgenciaTag(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CocineroCard(pedido: Pedido, onCambiarEstado: (EstadoPedido) -> Unit) {
    val urgenciaColor = when (pedido.minutosEntrega) {
        null -> Color(0xFF9E9E9E)
        in 0..35 -> Color(0xFFE53935)
        45 -> Color(0xFFFDD835)
        else -> Color(0xFF43A047)
    }

    Card(
        modifier = Modifier.fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = Color(0xFF007E67).copy(alpha = 0.1f)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Barra urgencia
            Box(modifier = Modifier.fillMaxWidth().height(5.dp).background(urgenciaColor))

            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pedido #${pedido.id}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                    pedido.minutosEntrega?.let {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                                .background(urgenciaColor.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("$it min", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = urgenciaColor)
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoChip(pedido.modulo.name)
                    InfoChip("👤 ${pedido.cliente?.nombre ?: "Sin nombre"}")
                }

                if (pedido.notas?.isNotBlank() == true) {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF3E0)).padding(8.dp)
                    ) {
                        Text("📝 ${pedido.notas}", fontSize = 12.sp, color = Color(0xFFE65100))
                    }
                }

                if (pedido.detalles.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Productos", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMid)
                        pedido.detalles.forEach { detalle ->
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Green).align(Alignment.CenterVertically))
                                Text("${detalle.cantidad}× ${detalle.nombreProducto}", fontSize = 13.sp, color = TextDark)
                            }
                        }
                    }
                }

                // Botón acción
                val (siguiente, btnLabel) = when (pedido.estado) {
                    EstadoPedido.PENDIENTE -> EstadoPedido.EN_PREPARACION to "Iniciar preparación"
                    EstadoPedido.EN_PREPARACION -> EstadoPedido.LISTO to "Marcar como listo"
                    else -> null to null
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EstadoChip(pedido.estado)
                    if (siguiente != null && btnLabel != null) {
                        Button(
                            onClick = { onCambiarEstado(siguiente) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            elevation = ButtonDefaults.buttonElevation(4.dp)
                        ) {
                            Text(btnLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFE6F4F1))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) { Text(text, fontSize = 11.sp, color = Color(0xFF007E67), fontWeight = FontWeight.Medium) }
}

@Composable
private fun EstadoChip(estado: EstadoPedido) {
    val (color, label) = when (estado) {
        EstadoPedido.PENDIENTE      -> Color(0xFFFF9800) to "Pendiente"
        EstadoPedido.EN_PREPARACION -> Color(0xFF1565C0) to "En preparación"
        EstadoPedido.LISTO          -> Color(0xFF2E7D32) to "Listo ✓"
        EstadoPedido.ENTREGADO      -> Color(0xFF9E9E9E) to "Entregado"
        EstadoPedido.CANCELADO      -> Color(0xFFC62828) to "Cancelado"
    }
    Box(
        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 5.dp)
    ) { Text(label, fontSize = 12.sp, color = color, fontWeight = FontWeight.SemiBold) }
}