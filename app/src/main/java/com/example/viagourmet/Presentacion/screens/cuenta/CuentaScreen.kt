package com.example.viagourmet.Presentacion.screens.cuenta

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.viagourmet.Presentacion.components.CantidadSelector
import com.example.viagourmet.data.repository.ItemCarrito

private val Green       = Color(0xFF007E67)
private val GreenDark   = Color(0xFF005C4B)
private val GreenPale   = Color(0xFFE6F4F1)
private val GreenMint   = Color(0xFFF0FAF7)
private val TextDark    = Color(0xFF0D2B24)
private val TextMid     = Color(0xFF4A7A6F)
private val TextLight   = Color(0xFF8AADA7)
private val CardBg      = Color(0xFFFFFFFF)
private val ErrorRed    = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuentaScreen(
    viewModel: CuentaViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSeguirComprando: () -> Unit,
    onVerEstadoPedido: (Int) -> Unit,
    onNavigateToPerfil: () -> Unit // ACCIÓN PARA IR AL PERFIL
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
        containerColor = GreenMint,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Green, GreenDark)))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)).clickable { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Default.ArrowBack, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
                    Column {
                        Text("Resumen de Cuenta", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Gestiona tu pedido y perfil", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        },
        bottomBar = {
            if (uiState.items.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .background(CardBg)
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = 13.sp, color = TextLight)
                            Text("$${"%.2f".format(uiState.subtotal)}", fontSize = 13.sp, color = TextMid)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("IVA (16%)", fontSize = 13.sp, color = TextLight)
                            Text("$${"%.2f".format(uiState.iva)}", fontSize = 13.sp, color = TextMid)
                        }
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFCCE8E2)))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Total", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text("$${"%.2f".format(uiState.total)}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Green)
                        }
                        Spacer(Modifier.height(4.dp))
                        Button(
                            onClick = { viewModel.onEvent(CuentaEvent.ConfirmarPedido) },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            enabled = !uiState.isLoading && uiState.horaSeleccionada != null,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Green, disabledContainerColor = Green.copy(alpha = 0.35f)),
                            elevation = ButtonDefaults.buttonElevation(8.dp, 2.dp)
                        ) {
                            if (uiState.isLoading) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                            else Text(
                                if (uiState.horaSeleccionada == null) "Selecciona hora de recogida" else "✓ Confirmar pedido",
                                fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECCIÓN DE PERFIL (ACCESO RÁPIDO)
            item {
                val sesion = viewModel.sessionManager.obtenerSesion()
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNavigateToPerfil() },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Miniatura de foto
                        Box(
                            modifier = Modifier.size(60.dp).clip(CircleShape).background(GreenPale),
                            contentAlignment = Alignment.Center
                        ) {
                            if (sesion?.fotoUri != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(sesion.fotoUri),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(Icons.Default.Person, null, tint = Green, modifier = Modifier.size(30.dp))
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Hola, ${sesion?.nombre ?: "Usuario"}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Text("Ver mi perfil y datos", fontSize = 12.sp, color = TextLight)
                        }
                        Text("Ver →", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Green)
                    }
                }
            }

            // SECCIÓN DE HORARIOS (Solo si hay items o para configuración)
            if (uiState.items.isNotEmpty()) {
                item {
                    Text("¿Cuándo lo recoges?", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OpcionHorario.entries.forEach { opcion ->
                            val isSelected = uiState.horaSeleccionada == opcion
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { viewModel.onEvent(CuentaEvent.SeleccionarHorario(opcion)) },
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = if (isSelected) GreenPale else CardBg),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Green) else null
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(when (opcion) {
                                            OpcionHorario.AHORA -> "⚡"
                                            OpcionHorario.MINUTOS_15 -> "🕐"
                                            OpcionHorario.MINUTOS_30 -> "🕧"
                                            OpcionHorario.MINUTOS_45 -> "🕑"
                                            OpcionHorario.UNA_HORA  -> "🕒"
                                        }, fontSize = 20.sp)
                                        Text(opcion.label, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Green else TextDark)
                                    }
                                    if (isSelected) {
                                        Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Green), contentAlignment = Alignment.Center) {
                                            Text("✓", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.items.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("🛒", fontSize = 56.sp)
                            Text("No tienes productos pendientes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
                            Button(onClick = onSeguirComprando, shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                                Text("Explorar el menú", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                item {
                    Text("Productos (${uiState.items.size})", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                }
                items(uiState.items, key = { it.id }) { item ->
                    CarritoItemCard(
                        item = item,
                        onEliminar = { viewModel.onEvent(CuentaEvent.EliminarItem(item.id)) },
                        onCantidadChange = { viewModel.onEvent(CuentaEvent.ActualizarCantidad(item.id, it)) }
                    )
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun CarritoItemCard(item: ItemCarrito, onEliminar: () -> Unit, onCantidadChange: (Int) -> Unit) {
    val subtotal = item.producto.precio.multiply(java.math.BigDecimal(item.cantidad))
    Card(
        modifier = Modifier.fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = Color(0xFF007E67).copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(GreenPale),
                contentAlignment = Alignment.Center
            ) { Text("🍽", fontSize = 22.sp) }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.producto.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextDark)
                Text("$${"%.2f".format(item.producto.precio)} c/u", fontSize = 12.sp, color = TextLight)
                Text("Subtotal: $${"%.2f".format(subtotal)}", fontSize = 12.sp, color = Green, fontWeight = FontWeight.SemiBold)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier.size(28.dp).clip(CircleShape).background(ErrorRed.copy(alpha = 0.1f)).clickable { onEliminar() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Delete, null, tint = ErrorRed, modifier = Modifier.size(15.dp)) }
                CantidadSelector(cantidad = item.cantidad, onCantidadChange = onCantidadChange, minCantidad = 1, maxCantidad = 10)
            }
        }
    }
}
