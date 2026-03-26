package com.example.viagourmet.Presentacion.screens.mipedido

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.viagourmet.Presentacion.theme.Brown80
import com.example.viagourmet.Presentacion.theme.GreenSuccess
import com.example.viagourmet.domain.model.EstadoPedido
import com.example.viagourmet.domain.model.Pedido

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiPedidoScreen(
    viewModel: MiPedidoViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estado de mi pedido") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Brown80,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.noPedidoActivo -> {
                    val entregado = uiState.ultimoEntregado
                    if (entregado != null) {
                        PedidoContenido(
                            pedido = entregado,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Sin ningún pedido
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("🍽️", style = MaterialTheme.typography.displayMedium)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No tienes pedidos activos",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "Haz un pedido desde el menú para verlo aquí",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = onNavigateBack) {
                                Text("Ir al menú")
                            }
                        }
                    }
                }

                else -> {
                    PedidoContenido(
                        pedido = uiState.pedidoActivo!!,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun PedidoContenido(pedido: Pedido, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Número de pedido
        Text(
            text = "Pedido #${pedido.id}",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Brown80
        )

        // Hora de recogida
        pedido.notas?.let { nota ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Brown80.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🕐", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = nota,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Brown80,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Timeline de estados
        Text(
            text = "Estado del pedido",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        val estadosTimeline = listOf(
            EstadoPedido.PENDIENTE to "Pedido recibido",
            EstadoPedido.EN_PREPARACION to "En preparación",
            EstadoPedido.LISTO to "Listo para recoger"
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                estadosTimeline.forEachIndexed { index, (estado, label) ->
                    val indexActual = estadosTimeline.indexOfFirst { it.first == pedido.estado }
                    val isPasado = index <= indexActual
                    val isActual = pedido.estado == estado

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = MaterialTheme.shapes.extraLarge,
                            color = when {
                                isActual -> GreenSuccess
                                isPasado -> Brown80
                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            },
                            modifier = Modifier.size(22.dp)
                        ) {
                            if (isPasado && !isActual) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "✓",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isActual) FontWeight.Bold else FontWeight.Normal,
                            color = when {
                                isActual -> GreenSuccess
                                isPasado -> MaterialTheme.colorScheme.onSurface
                                else -> MaterialTheme.colorScheme.outline
                            },
                            modifier = Modifier.weight(1f)
                        )

                        if (isActual) {
                            Surface(
                                color = GreenSuccess.copy(alpha = 0.15f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "Actual",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GreenSuccess,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (index < estadosTimeline.lastIndex) {
                        Box(
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .width(2.dp)
                                .height(24.dp)
                        ) {
                            Surface(
                                color = if (index < indexActual) Brown80
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                modifier = Modifier.fillMaxSize()
                            ) {}
                        }
                    }
                }
            }
        }

        // Banner de estado especial
        when (pedido.estado) {
            EstadoPedido.CANCELADO -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "tu pedido ha sido cancelado",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            EstadoPedido.LISTO -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = GreenSuccess.copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = "Tu pedido está listo, por favor pasa a recogerlo.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = GreenSuccess,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            EstadoPedido.ENTREGADO -> {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = " Pedido entregado. disfrutalo",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            else -> {}
        }

        // Resumen de productos
        if (pedido.detalles.isNotEmpty()) {
            Text(
                text = "Resumen",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pedido.detalles.forEach { detalle ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${detalle.cantidad}x ${detalle.producto?.nombre ?: "Producto"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "$${"%.2f".format(detalle.subtotal)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Brown80,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text(
                            text = "$${"%.2f".format(pedido.detalles.sumOf { it.subtotal })}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Brown80
                        )
                    }
                }
            }
        }
    }
}