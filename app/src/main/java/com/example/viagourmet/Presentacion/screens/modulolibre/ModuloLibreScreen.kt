package com.example.viagourmet.Presentacion.screens.modulolibre

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.viagourmet.Presentacion.theme.Brown80

val opcionesTiempo = listOf(
    25 to "25 minutos",
    30 to "30 minutos",
    35 to "35 minutos",
    45 to "45 minutos",
    60 to "1 hora"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuloLibreScreen(
    viewModel: ModuloLibreViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onPedidoEnviado: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.pedidoEnviado) {
        if (uiState.pedidoEnviado) onPedidoEnviado()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pedido Libre", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Brown80)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Describe tu pedido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Escribe con detalle lo que deseas. Por ejemplo: '2 tortas de jamón sin chile, 1 café americano grande'",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = { viewModel.onDescripcionChange(it) },
                label = { Text("Tu pedido libre") },
                placeholder = { Text("Escribe aquí tu pedido...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 6
            )

            Text(
                "¿Cuándo lo quieres?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            opcionesTiempo.forEach { (minutos, etiqueta) ->
                val urgencia = when {
                    minutos <= 35 -> "🔴 Urgente"
                    minutos == 45 -> "🟡 Prioridad media"
                    else -> "🟢 Sin prisa"
                }
                OutlinedCard(
                    onClick = { viewModel.onTiempoSeleccionado(minutos) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (uiState.minutosSeleccionados == minutos)
                        CardDefaults.outlinedCardColors(containerColor = Brown80.copy(alpha = 0.1f))
                    else CardDefaults.outlinedCardColors(),
                    border = if (uiState.minutosSeleccionados == minutos)
                        CardDefaults.outlinedCardBorder().copy()
                    else CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(etiqueta, fontWeight = FontWeight.Medium)
                            Text(urgencia, style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline)
                        }
                        RadioButton(
                            selected = uiState.minutosSeleccionados == minutos,
                            onClick = { viewModel.onTiempoSeleccionado(minutos) }
                        )
                    }
                }
            }

            // Opción de pago: cuenta pendiente
            Text("Método de pago", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = uiState.cuentaPendiente,
                    onCheckedChange = { viewModel.onCuentaPendienteChange(it) }
                )
                Column {
                    Text("Cuenta pendiente", style = MaterialTheme.typography.bodyMedium)
                    Text("Pagar después en caja", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline)
                }
            }

            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = { viewModel.enviarPedido() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.descripcion.isNotBlank() && uiState.minutosSeleccionados != null,
                colors = ButtonDefaults.buttonColors(containerColor = Brown80)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Enviar pedido libre")
                }
            }
        }
    }
}
