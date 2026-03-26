package com.example.viagourmet.Presentacion.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.viagourmet.Presentacion.theme.Brown80
import com.example.viagourmet.data.mock.MockData
import com.example.viagourmet.domain.model.Producto
import com.example.viagourmet.Presentacion.components.CantidadSelector


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoDetalleScreen(
    productoId: Int,
    onNavigateBack: () -> Unit,
    onAgregarAlPedido: (Producto, Int) -> Unit
) {
    // Buscar producto
    val producto = remember(productoId) {
        MockData.productos.find { it.id == productoId }
    }

    var cantidad by remember { mutableIntStateOf(1) }

    if (producto == null) {
        // Producto no encontrado
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Producto no encontrado")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateBack) {
                    Text("Regresar")
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(producto.nombre) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Brown80,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Precio total
                    Text(
                        text = "Total: $${"%.2f".format(producto.precio * cantidad.toBigDecimal())}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Brown80
                    )

                    // Botón agregar
                    Button(
                        onClick = {
                            onAgregarAlPedido(producto, cantidad)
                            onNavigateBack()
                        },
                        enabled = producto.disponible
                    ) {
                        Text("Agregar")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Imagen
            AsyncImage(
                model = producto.imagenUrl ?: "https://via.placeholder.com/400x200/FDF5E6/8B5A2B?text=${producto.nombre}",
                contentDescription = producto.nombre,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            // Información
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Nombre y precio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = producto.nombre,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Text(
                        text = "$${"%.2f".format(producto.precio)} c/u",
                        style = MaterialTheme.typography.titleLarge,
                        color = Brown80
                    )
                }

                // Descripción
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = producto.descripcion,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // Disponibilidad
                if (!producto.disponible) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "Producto no disponible temporalmente",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // Selector de cantidad
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Cantidad",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        CantidadSelector(
                            cantidad = cantidad,
                            onCantidadChange = { cantidad = it },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}