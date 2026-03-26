package com.example.viagourmet.Presentacion.screens.cocinero

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.viagourmet.Presentacion.theme.Brown80
import com.example.viagourmet.domain.model.EstadoPedido
import com.example.viagourmet.domain.model.Pedido
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocinerosScreen(
    viewModel: CocinerosViewModel = hiltViewModel(),
    onCerrarSesion: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cocina", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Brown80),
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Salir", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Leyenda de colores
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UrgenciaChip(Color(0xFFE53935), "🔴 Urgente (≤35 min)")
                UrgenciaChip(Color(0xFFFDD835), "🟡 Medio (45 min)")
                UrgenciaChip(Color(0xFF43A047), "🟢 Normal (60 min)")
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.pedidos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay pedidos activos", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.pedidos, key = { it.id }) { pedido ->
                        PedidoCocineroCard(
                            pedido = pedido,
                            onCambiarEstado = { nuevoEstado ->
                                viewModel.cambiarEstado(pedido.id, nuevoEstado)
                            }
                        )
                    }
                }
            }

            uiState.errorMessage?.let {
                Snackbar(modifier = Modifier.padding(16.dp)) { Text(it) }
            }
        }
    }
}

@Composable
private fun UrgenciaChip(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(5.dp))
        )
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun PedidoCocineroCard(
    pedido: Pedido,
    onCambiarEstado: (EstadoPedido) -> Unit
) {
    val urgenciaColor = when (pedido.minutosEntrega) {
        null -> Color(0xFF9E9E9E)
        in 0..35 -> Color(0xFFE53935)   // Rojo — urgente
        45 -> Color(0xFFFDD835)          // Amarillo — medio
        else -> Color(0xFF43A047)        // Verde — normal
    }

    val fmt = DateTimeFormatter.ofPattern("HH:mm")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Barra de color urgencia
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(urgenciaColor)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Pedido #${pedido.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    pedido.minutosEntrega?.let {
                        Surface(
                            color = urgenciaColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "${it} min",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = urgenciaColor.copy(alpha = 1f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                Text("Módulo: ${pedido.modulo.name}", style = MaterialTheme.typography.bodyMedium)
                Text("Cliente: ${pedido.cliente?.nombre ?: "Sin cliente"}", style = MaterialTheme.typography.bodySmall)
                Text("Hora: ${pedido.creadoEn.format(fmt)}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline)

                if (pedido.notas?.isNotBlank() == true) {
                    Text("Notas: ${pedido.notas}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary)
                }

                if (pedido.detalles.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text("Productos:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    pedido.detalles.forEach { detalle ->
                        Text("  • ${detalle.cantidad}x ${detalle.nombreProducto}",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Botones cambiar estado
                val siguienteEstado = when (pedido.estado) {
                    EstadoPedido.PENDIENTE -> EstadoPedido.EN_PREPARACION
                    EstadoPedido.EN_PREPARACION -> EstadoPedido.LISTO
                    else -> null
                }
                val estadoLabel = when (pedido.estado) {
                    EstadoPedido.PENDIENTE -> "Iniciar preparación"
                    EstadoPedido.EN_PREPARACION -> "Marcar como listo"
                    EstadoPedido.LISTO -> "✅ Listo para entrega"
                    else -> null
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EstadoBadge(pedido.estado)
                    siguienteEstado?.let { estado ->
                        Button(
                            onClick = { onCambiarEstado(estado) },
                            colors = ButtonDefaults.buttonColors(containerColor = Brown80)
                        ) {
                            Text(estadoLabel ?: "Avanzar", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadoBadge(estado: EstadoPedido) {
    val (color, label) = when (estado) {
        EstadoPedido.PENDIENTE -> Color(0xFFFF9800) to "Pendiente"
        EstadoPedido.EN_PREPARACION -> Color(0xFF2196F3) to "En preparación"
        EstadoPedido.LISTO -> Color(0xFF4CAF50) to "Listo"
        EstadoPedido.ENTREGADO -> Color(0xFF9E9E9E) to "Entregado"
        EstadoPedido.CANCELADO -> Color(0xFFF44336) to "Cancelado"
    }
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall, color = color)
    }
}
