package com.example.viagourmet.Presentacion.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.viagourmet.Presentacion.components.CategoriaChip
import com.example.viagourmet.Presentacion.components.ProductoCard
import com.example.viagourmet.Presentacion.theme.Brown80
import com.example.viagourmet.domain.model.ModuloPedido

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: MenuViewModel = hiltViewModel(),
    onNavigateToDetalle: (Int) -> Unit,
    onNavigateToCuenta: () -> Unit,
    onNavigateToMiPedido: () -> Unit,
    onNavigateToModuloLibre: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var moduloSeleccionado by remember { mutableStateOf(ModuloPedido.DESAYUNOS) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro que quieres cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onCerrarSesion() }) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cafetería", color = MaterialTheme.colorScheme.onPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Brown80),
                actions = {
                    IconButton(onClick = onNavigateToMiPedido) {
                        Icon(Icons.Outlined.CheckCircle, contentDescription = "Mi pedido",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = onNavigateToCuenta) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Salir",
                            tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // ── Selector de módulo ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ModuloPedido.values().forEach { modulo ->
                    val isSelected = moduloSeleccionado == modulo
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (modulo == ModuloPedido.LIBRE) {
                                onNavigateToModuloLibre()
                            } else {
                                moduloSeleccionado = modulo
                                viewModel.onModuloChange(modulo)
                            }
                        },
                        label = {
                            Text(
                                when (modulo) {
                                    ModuloPedido.DESAYUNOS -> "🌅 Desayunos"
                                    ModuloPedido.COMIDAS   -> "🍽️ Comidas"
                                    ModuloPedido.LIBRE     -> "✏️ Libre"
                                },
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Filtro de categorías
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        CategoriaChip(
                            nombre = "Todos",
                            seleccionado = uiState.categoriaSeleccionada == null,
                            onClick = { viewModel.onCategoriaChange(null) }
                        )
                    }
                    items(uiState.categorias) { cat ->
                        CategoriaChip(
                            nombre = cat.nombre,
                            seleccionado = uiState.categoriaSeleccionada?.id == cat.id,
                            onClick = { viewModel.onCategoriaChange(cat) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Productos
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.productosFiltrados) { producto ->
                        ProductoCard(
                            producto = producto,
                            onClick = { onNavigateToDetalle(producto.id) }
                        )
                    }
                    if (uiState.productosFiltrados.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No hay productos disponibles",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }

            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp))
            }
        }
    }
}
