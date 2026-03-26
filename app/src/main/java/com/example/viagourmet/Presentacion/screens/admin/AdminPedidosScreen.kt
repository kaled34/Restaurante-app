package com.example.viagourmet.Presentacion.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPedidosScreen(
    viewModel: AdminPedidosViewModel = hiltViewModel(),
    onCerrarSesion: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.mensajeExito) {
        uiState.mensajeExito?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.onEvent(AdminEvent.LimpiarMensaje)
            }
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.onEvent(AdminEvent.LimpiarMensaje)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Panel Admin",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Gestión de pedidos",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Brown80),
                actions = {
                    IconButton(onClick = { viewModel.onEvent(AdminEvent.Cargar) }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Recargar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = onCerrarSesion) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            ResumenContadores(uiState = uiState)

            ScrollableTabRow(
                selectedTabIndex = FiltroAdmin.entries.indexOf(uiState.filtroSeleccionado),
                containerColor = MaterialTheme.colorScheme.surface,
                edgePadding = 8.dp
            ) {
                FiltroAdmin.entries.forEach { filtro ->
                    Tab(
                        selected = uiState.filtroSeleccionado == filtro,
                        onClick = { viewModel.onEvent(AdminEvent.SeleccionarFiltro(filtro)) },
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(filtro.label)
                                val count = when (filtro) {
                                    FiltroAdmin.PENDIENTE -> uiState.contadorPendientes
                                    FiltroAdmin.EN_PREPARACION -> uiState.contadorEnPreparacion
                                    FiltroAdmin.LISTO -> uiState.contadorListos
                                    else -> 0
                                }
                                if (count > 0) {
                                    Badge { Text(count.toString()) }
                                }
                            }
                        }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    uiState.pedidosFiltrados.isEmpty() -> {
                        EmptyState(
                            filtro = uiState.filtroSeleccionado,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = uiState.pedidosFiltrados,
                                key = { it.id }
                            ) { pedido ->
                                // Tarjeta de pedido
                                PedidoAdminCard(
                                    pedido = pedido,
                                    onClick = { viewModel.onEvent(AdminEvent.VerDetalle(pedido)) },
                                    onAvanzarEstado = {
                                        pedido.estado.siguienteEstado()?.let { siguiente ->
                                            viewModel.onEvent(
                                                AdminEvent.CambiarEstado(pedido.id, siguiente)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Pedido seleccionado actual
    val pedidoActual = uiState.pedidoSeleccionado
    if (uiState.showDetalle && pedidoActual != null) {
        PedidoDetalleSheet(
            pedido = pedidoActual,
            isActualizando = uiState.isActualizando,
            onDismiss = { viewModel.onEvent(AdminEvent.CerrarDetalle) },
            onCambiarEstado = { nuevoEstado ->
                viewModel.onEvent(
                    AdminEvent.CambiarEstado(pedidoActual.id, nuevoEstado)
                )
            }
        )
    }
}

@Composable
private fun ResumenContadores(uiState: AdminPedidosUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ContadorChip(
            label = "Pendientes",
            count = uiState.contadorPendientes,
            color = EstadoPedido.PENDIENTE.colorBorde(),
            modifier = Modifier.weight(1f)
        )
        ContadorChip(
            label = "En prep.",
            count = uiState.contadorEnPreparacion,
            color = EstadoPedido.EN_PREPARACION.colorBorde(),
            modifier = Modifier.weight(1f)
        )
        ContadorChip(
            label = "Listos",
            count = uiState.contadorListos,
            color = EstadoPedido.LISTO.colorBorde(),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ContadorChip(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun EmptyState(filtro: FiltroAdmin, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = when (filtro) {
                FiltroAdmin.PENDIENTE -> "Pendiente y luego el diente"
                FiltroAdmin.EN_PREPARACION -> "En preparación"
                FiltroAdmin.LISTO -> "Listo!!"
                FiltroAdmin.HISTORIAL -> "Historial"
                FiltroAdmin.TODOS -> "Todos"
            },
            style = MaterialTheme.typography.displaySmall
        )
        Text(
            text = when (filtro) {
                FiltroAdmin.PENDIENTE -> "Sin pedidos pendientes"
                FiltroAdmin.EN_PREPARACION -> "Nada en preparación"
                FiltroAdmin.LISTO -> "No hay pedidos listos"
                FiltroAdmin.HISTORIAL -> "Historial vacío"
                FiltroAdmin.TODOS -> "No hay pedidos activos"
            },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}