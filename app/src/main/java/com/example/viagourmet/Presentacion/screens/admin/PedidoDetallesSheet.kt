package com.example.viagourmet.Presentacion.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viagourmet.Presentacion.theme.Brown80
import com.example.viagourmet.domain.model.EstadoPedido
import com.example.viagourmet.domain.model.Pedido
import com.example.viagourmet.domain.model.TipoPedido
import java.time.LocalDateTime
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoDetalleSheet(
    pedido: Pedido,
    isActualizando: Boolean,
    onDismiss: () -> Unit,
    onCambiarEstado: (EstadoPedido) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Pedido #${pedido.id}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = pedido.creadoEn.toFechaHoraString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar")
                }
            }

            // Estado actual
            Surface(
                color = pedido.estado.colorFondo(),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = pedido.estado.icono(), style = MaterialTheme.typography.titleLarge)
                    Column {
                        Text(
                            text = "Estado actual",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = pedido.estado.displayName(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = pedido.estado.colorTexto()
                        )
                    }
                }
            }

            HorizontalDivider()

            // Info del cliente
            pedido.cliente?.let { cliente ->
                SectionTitle("Cliente")
                InfoRow("Nombre", "${cliente.nombre} ${cliente.apellido ?: ""}")
                cliente.telefono?.let { InfoRow("Teléfono", it) }
                cliente.email?.let { InfoRow("Email", it) }
            }

            HorizontalDivider()

            // Info del pedido
            SectionTitle("Detalles del pedido")
            InfoRow(
                "Módulo",
                pedido.modulo.displayName()
            )
            InfoRow(
                "Tipo",
                if (pedido.tipo == TipoPedido.PARA_LLEVAR) "Para llevar" else "Oficina"
            )

            // Productos del pedido
            if (pedido.detalles.isNotEmpty()) {
                SectionTitle("Productos")
                pedido.detalles.forEach { detalle ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${detalle.cantidad}x ${detalle.producto?.nombre ?: "Producto #${detalle.productoId}"}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$${"%.2f".format(detalle.subtotal)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Brown80
                        )
                    }
                    detalle.notas?.let {
                        Text(
                            text = "  ↳ Nota: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            // Items libres
            if (pedido.itemsLibres.isNotEmpty()) {
                SectionTitle("Items especiales")
                pedido.itemsLibres.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${item.cantidad}x ${item.descripcion}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$${"%.2f".format(item.subtotal)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Brown80
                        )
                    }
                    item.notas?.let {
                        Text(
                            text = "  ↳ $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Total
            val totalProductos = pedido.detalles.sumOf { it.subtotal }
            val totalLibres = pedido.itemsLibres.sumOf { it.subtotal }
            val total = totalProductos + totalLibres
            if (total > java.math.BigDecimal.ZERO) {
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        text = "$${"%.2f".format(total)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Brown80
                    )
                }
            }

            // Notas
            if (!pedido.notas.isNullOrBlank()) {
                HorizontalDivider()
                SectionTitle("Notas especiales")
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = pedido.notas,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            HorizontalDivider()

            // Acciones de estado
            SectionTitle("Cambiar estado")

            if (isActualizando) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            } else {
                // Botones de cambio de estado
                val estadosAccion = buildList {
                    val siguiente = pedido.estado.siguienteEstado()
                    if (siguiente != null) add(siguiente)
                    if (pedido.estado != EstadoPedido.CANCELADO && pedido.estado != EstadoPedido.ENTREGADO) {
                        add(EstadoPedido.CANCELADO)
                    }
                }

                estadosAccion.forEach { estado ->
                    val esCancelar = estado == EstadoPedido.CANCELADO
                    Button(
                        onClick = { onCambiarEstado(estado) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (esCancelar)
                                MaterialTheme.colorScheme.errorContainer
                            else
                                estado.colorBorde().copy(alpha = 0.9f),
                            contentColor = if (esCancelar)
                                MaterialTheme.colorScheme.onErrorContainer
                            else
                                Color.White
                        )
                    ) {
                        Text(
                            text = "${estado.icono()} Marcar como ${estado.displayName()}",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}