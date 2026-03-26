package com.example.viagourmet.Presentacion.screens.cuenta

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.viagourmet.Presentacion.components.CantidadSelector
import com.example.viagourmet.Presentacion.theme.Brown80
import com.example.viagourmet.data.repository.ItemCarrito

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuentaScreen(
    viewModel: CuentaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSeguirComprando: () -> Unit,
    onVerEstadoPedido: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.pedidoConfirmado) {
        uiState.pedidoConfirmado?.let { pedido ->
            onVerEstadoPedido(pedido.id)
            viewModel.onEvent(CuentaEvent.LimpiarCuenta)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(CuentaEvent.LimpiarMensaje)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi pedido") },
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.items.isNotEmpty()) {
                Surface(tonalElevation = 4.dp) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal", style = MaterialTheme.typography.bodyMedium)
                            Text("$${"%.2f".format(uiState.subtotal)}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("IVA (16%)", style = MaterialTheme.typography.bodyMedium)
                            Text("$${"%.2f".format(uiState.iva)}", style = MaterialTheme.typography.bodyMedium)
                        }
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "$${"%.2f".format(uiState.total)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Brown80
                            )
                        }

                        Button(
                            onClick = { viewModel.onEvent(CuentaEvent.ConfirmarPedido) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading && uiState.horaSeleccionada != null,
                            colors = ButtonDefaults.buttonColors(containerColor = Brown80)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (uiState.horaSeleccionada == null)
                                        "Selecciona una hora de recogida"
                                    else "Confirmar pedido"
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->

        if (uiState.items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("🛒", style = MaterialTheme.typography.displayMedium)
                    Text("Tu pedido está vacío", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Agrega productos desde el menú",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onSeguirComprando) { Text("Ver menú") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // Selector de horario
                item {
                    Text(
                        "¿Cuándo quieres recoger?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OpcionHorario.entries.forEach { opcion ->
                            HorarioOpcionItem(
                                opcion = opcion,
                                isSelected = uiState.horaSeleccionada == opcion,
                                onClick = { viewModel.onEvent(CuentaEvent.SeleccionarHorario(opcion)) }
                            )
                        }
                    }
                }

                item { HorizontalDivider() }

                item {
                    Text(
                        "Productos (${uiState.items.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.items, key = { it.id }) { item ->
                    ItemCarritoRow(
                        item = item,
                        onEliminar = { viewModel.onEvent(CuentaEvent.EliminarItem(item.id)) },
                        onCantidadChange = { nueva ->
                            viewModel.onEvent(CuentaEvent.ActualizarCantidad(item.id, nueva))
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun HorarioOpcionItem(
    opcion: OpcionHorario,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) Brown80.copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Brown80 else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (opcion) {
                    OpcionHorario.AHORA -> ""
                    OpcionHorario.MINUTOS_15 -> ""
                    OpcionHorario.MINUTOS_30 -> ""
                    OpcionHorario.MINUTOS_45 -> ""
                    OpcionHorario.UNA_HORA -> ""
                },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = opcion.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Brown80 else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ItemCarritoRow(
    item: ItemCarrito,
    onEliminar: () -> Unit,
    onCantidadChange: (Int) -> Unit
) {
    val subtotal = item.producto.precio.multiply(java.math.BigDecimal(item.cantidad))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.producto.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$${"%.2f".format(item.producto.precio)} c/u",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Subtotal: $${"%.2f".format(subtotal)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Brown80,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onEliminar, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
                CantidadSelector(
                    cantidad = item.cantidad,
                    onCantidadChange = onCantidadChange,
                    minCantidad = 1,
                    maxCantidad = 10
                )
            }
        }
    }
}