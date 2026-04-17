package com.example.viagourmet.Presentacion.screens.admin

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.viagourmet.domain.model.EstadoPedido
import com.example.viagourmet.domain.model.Pedido

@Composable
fun PedidoAdminCard(
    pedido: Pedido,
    onClick: () -> Unit,
    onAvanzarEstado: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estadoColor by animateColorAsState(
        targetValue = pedido.estado.colorFondo(),
        animationSpec = tween(300),
        label = "estadoColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.5.dp, pedido.estado.colorBorde()),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(estadoColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pedido.estado.icono(),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = pedido.estado.displayName(),
                        style = MaterialTheme.typography.labelLarge,
                        color = pedido.estado.colorTexto(),
                        fontWeight = FontWeight.Bold
                    )
                }

                Surface(
                    color = pedido.modulo.colorFondo(),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = pedido.modulo.displayName(),
                        style = MaterialTheme.typography.labelSmall,
                        color = pedido.modulo.colorTexto(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }


            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Número y hora
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pedido #${pedido.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = pedido.creadoEn.toHoraString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Cliente
                pedido.cliente?.let { cliente ->
                    Text(
                        text = "👤 ${cliente.nombre} ${cliente.apellido ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Resumen de productos
                val resumen = buildString {
                    val items = pedido.detalles.mapNotNull { it.producto?.nombre }
                        .plus(pedido.itemsLibres.map { it.descripcion })
                    append(items.take(2).joinToString(", "))
                    if (items.size > 2) append(" +${items.size - 2} más")
                }
                if (resumen.isNotBlank()) {
                    Text(
                        text = resumen,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Notas
                if (!pedido.notas.isNullOrBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFFE65100)
                        )
                        Text(
                            text = pedido.notas,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFE65100),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Tipo y estado siguiente
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (pedido.tipo.name == "PARA_LLEVAR") "🛍 Para llevar" else "🏢 Oficina",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val siguiente = pedido.estado.siguienteEstado()
                    if (siguiente != null) {
                        FilledTonalButton(
                            onClick = onAvanzarEstado,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = siguiente.colorFondo().copy(alpha = 0.9f),
                                contentColor = siguiente.colorTexto()
                            )
                        ) {
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = siguiente.displayName(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }
    }
}