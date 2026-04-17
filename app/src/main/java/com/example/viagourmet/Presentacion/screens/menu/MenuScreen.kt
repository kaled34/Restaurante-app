package com.example.viagourmet.Presentacion.screens.menu

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.viagourmet.domain.model.ModuloPedido


// Paleta CONALEP
private val Green     = Color(0xFF007E67)
private val GreenDark = Color(0xFF005C4B)
private val GreenPale = Color(0xFFE6F4F1)
private val GreenMint = Color(0xFFF0FAF7)
private val TextDark  = Color(0xFF0D2B24)
private val TextMid   = Color(0xFF4A7A6F)
private val TextLight = Color(0xFF8AADA7)
private val CardBg    = Color(0xFFFFFFFF)
private val ErrorRed  = Color(0xFFD32F2F)

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
            containerColor = CardBg,
            shape = RoundedCornerShape(20.dp),
            title = { Text("Cerrar sesión", fontWeight = FontWeight.Bold, color = TextDark) },
            text  = { Text("¿Estás seguro que quieres salir?", color = TextMid) },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onCerrarSesion() }) {
                    Text("Salir", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = Green)
                }
            }
        )
    }

    Scaffold(
        containerColor = GreenMint,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                        Text("☕ Cafetería", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("CONALEP", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f), letterSpacing = 1.sp)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TopBarIconBtn(Icons.Outlined.CheckCircle, "Mi pedido") { onNavigateToMiPedido() }
                        TopBarIconBtn(Icons.Default.ShoppingCart, "Carrito")   { onNavigateToCuenta() }
                        TopBarIconBtn(Icons.Default.ExitToApp, "Salir")        { showLogoutDialog = true }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Selector de módulo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(GreenDark, Green)))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ModuloPedido.values().forEach { modulo ->
                        val isSelected = moduloSeleccionado == modulo
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable {
                                    if (modulo == ModuloPedido.LIBRE) onNavigateToModuloLibre()
                                    else { moduloSeleccionado = modulo; viewModel.onModuloChange(modulo) }
                                }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (modulo) {
                                    ModuloPedido.DESAYUNOS -> "🌅 Desayunos"
                                    ModuloPedido.COMIDAS   -> "🍽 Comidas"
                                    ModuloPedido.LIBRE     -> "✏️ Libre"
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Green else Color.White
                            )
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green)
                }
            } else {
                // Chips de categoría
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        CategoriaChip(
                            nombre = "Todos",
                            isSelected = uiState.categoriaSeleccionada == null,
                            onClick = { viewModel.onCategoriaChange(null) }
                        )
                    }
                    items(uiState.categorias) { cat ->
                        CategoriaChip(
                            nombre = cat.nombre,
                            isSelected = uiState.categoriaSeleccionada?.id == cat.id,
                            onClick = { viewModel.onCategoriaChange(cat) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.productosFiltrados) { producto ->
                        ProductoCard(producto = producto, onClick = { onNavigateToDetalle(producto.id) })
                    }
                    if (uiState.productosFiltrados.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🍽", fontSize = 40.sp)
                                    Spacer(Modifier.height(8.dp))
                                    Text("No hay productos disponibles", color = TextLight, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }

            uiState.errorMessage?.let {
                Text(it, color = ErrorRed, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

// ── Componentes privados ─────────────────────────────────────────────────────

@Composable
private fun TopBarIconBtn(icon: ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.15f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = desc, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun CategoriaChip(nombre: String, isSelected: Boolean, onClick: () -> Unit) {
    val borderModifier = if (!isSelected)
        Modifier.border(BorderStroke(1.dp, Color(0xFFCCE8E2)), RoundedCornerShape(20.dp))
    else Modifier

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .then(borderModifier)
            .background(if (isSelected) Green else CardBg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = nombre,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) Color.White else TextMid
        )
    }
}

@Composable
private fun ProductoCard(producto: com.example.viagourmet.domain.model.Producto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = Green.copy(alpha = 0.1f))
            .clickable(enabled = producto.disponible) { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(14.dp)).background(GreenPale),
                contentAlignment = Alignment.Center
            ) {
                if (producto.imagenUrl != null) {
                    AsyncImage(
                        model = producto.imagenUrl,
                        contentDescription = producto.nombre,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = when {
                            producto.nombre.contains("café", true) || producto.nombre.contains("jugo", true) -> "☕"
                            producto.nombre.contains("hot", true) || producto.nombre.contains("pan", true)  -> "🥞"
                            producto.nombre.contains("pollo", true) || producto.nombre.contains("pechuga", true) -> "🍗"
                            producto.nombre.contains("pasta", true) -> "🍝"
                            producto.nombre.contains("hambur", true) -> "🍔"
                            else -> "🍽"
                        },
                        fontSize = 30.sp
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(producto.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(producto.descripcion, fontSize = 12.sp, color = TextLight, maxLines = 2, overflow = TextOverflow.Ellipsis)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("$${"%.2f".format(producto.precio)}", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Green)
                    if (!producto.disponible) {
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(ErrorRed.copy(alpha = 0.1f)).padding(horizontal = 8.dp, vertical = 3.dp)
                        ) { Text("Agotado", fontSize = 11.sp, color = ErrorRed, fontWeight = FontWeight.SemiBold) }
                    } else {
                        Box(
                            modifier = Modifier.size(28.dp).clip(CircleShape).background(Green),
                            contentAlignment = Alignment.Center
                        ) { Text("+", fontSize = 18.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}