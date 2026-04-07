package com.example.viagourmet.Presentacion.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.viagourmet.domain.model.EstadoPedido
import com.example.viagourmet.domain.model.ModuloPedido
import com.example.viagourmet.domain.model.Pedido
import com.example.viagourmet.domain.model.TipoPedido
import kotlinx.coroutines.launch

// ── Paleta CONALEP ───────────────────────────────────────────────────────────
private val Green     = Color(0xFF007E67)
private val GreenDark = Color(0xFF005C4B)
private val GreenLight= Color(0xFF00A882)
private val GreenPale = Color(0xFFE6F4F1)
private val GreenMint = Color(0xFFF0FAF7)
private val TextDark  = Color(0xFF0D2B24)
private val TextMid   = Color(0xFF4A7A6F)
private val TextLight = Color(0xFF8AADA7)
private val CardBg    = Color(0xFFFFFFFF)
private val ErrorRed  = Color(0xFFD32F2F)

// ── Extensiones de EstadoPedido (inline para no depender del archivo original) ──
private fun EstadoPedido.label(): String = when (this) {
    EstadoPedido.PENDIENTE      -> "Pendiente"
    EstadoPedido.EN_PREPARACION -> "En preparación"
    EstadoPedido.LISTO          -> "Listo"
    EstadoPedido.ENTREGADO      -> "Entregado"
    EstadoPedido.CANCELADO      -> "Cancelado"
}
private fun EstadoPedido.emoji(): String = when (this) {
    EstadoPedido.PENDIENTE      -> "🟡"
    EstadoPedido.EN_PREPARACION -> "🔵"
    EstadoPedido.LISTO          -> "🟢"
    EstadoPedido.ENTREGADO      -> "✅"
    EstadoPedido.CANCELADO      -> "❌"
}
private fun EstadoPedido.accentColor(): Color = when (this) {
    EstadoPedido.PENDIENTE      -> Color(0xFFFF8F00)
    EstadoPedido.EN_PREPARACION -> Color(0xFF1565C0)
    EstadoPedido.LISTO          -> Color(0xFF2E7D32)
    EstadoPedido.ENTREGADO      -> Color(0xFF757575)
    EstadoPedido.CANCELADO      -> Color(0xFFC62828)
}
private fun EstadoPedido.next(): EstadoPedido? = when (this) {
    EstadoPedido.PENDIENTE      -> EstadoPedido.EN_PREPARACION
    EstadoPedido.EN_PREPARACION -> EstadoPedido.LISTO
    EstadoPedido.LISTO          -> EstadoPedido.ENTREGADO
    else                        -> null
}
private fun ModuloPedido.label(): String = when (this) {
    ModuloPedido.DESAYUNOS -> "🌅 Desayunos"
    ModuloPedido.COMIDAS   -> "🍽 Comidas"
    ModuloPedido.LIBRE     -> "✏️ Libre"
}

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
            scope.launch { snackbarHostState.showSnackbar(it); viewModel.onEvent(AdminEvent.LimpiarMensaje) }
        }
    }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            scope.launch { snackbarHostState.showSnackbar(it); viewModel.onEvent(AdminEvent.LimpiarMensaje) }
        }
    }

    Scaffold(
        containerColor = GreenMint,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        Text("Panel Admin", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Gestión de pedidos · CONALEP", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AdminTopBtn(Icons.Default.Refresh) { viewModel.onEvent(AdminEvent.Cargar) }
                        AdminTopBtn(Icons.Default.ExitToApp) { onCerrarSesion() }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // ── Contadores ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(GreenDark, Green)))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CounterBadge("🟡", "Pendientes", uiState.contadorPendientes,  Modifier.weight(1f))
                    CounterBadge("🔵", "En prep.",   uiState.contadorEnPreparacion, Modifier.weight(1f))
                    CounterBadge("🟢", "Listos",     uiState.contadorListos,        Modifier.weight(1f))
                }
            }

            // ── Tabs de filtro ──────────────────────────────────────────────
            ScrollableTabRow(
                selectedTabIndex = FiltroAdmin.entries.indexOf(uiState.filtroSeleccionado),
                containerColor = CardBg,
                contentColor = Green,
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    val idx = FiltroAdmin.entries.indexOf(uiState.filtroSeleccionado)
                    if (idx < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[idx]),
                            color = Green
                        )
                    }
                }
            ) {
                FiltroAdmin.entries.forEach { filtro ->
                    val sel = uiState.filtroSeleccionado == filtro
                    Tab(
                        selected = sel,
                        onClick = { viewModel.onEvent(AdminEvent.SeleccionarFiltro(filtro)) },
                        selectedContentColor = Green,
                        unselectedContentColor = TextLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(filtro.label, fontSize = 13.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                            val count = when (filtro) {
                                FiltroAdmin.PENDIENTE      -> uiState.contadorPendientes
                                FiltroAdmin.EN_PREPARACION -> uiState.contadorEnPreparacion
                                FiltroAdmin.LISTO          -> uiState.contadorListos
                                else                       -> 0
                            }
                            if (count > 0) {
                                Box(
                                    modifier = Modifier.size(18.dp).clip(CircleShape).background(Green),
                                    contentAlignment = Alignment.Center
                                ) { Text(count.toString(), fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold) }
                            }
                        }
                    }
                }
            }

            // ── Lista de pedidos ────────────────────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Green)
                    }
                    uiState.pedidosFiltrados.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("📋", fontSize = 48.sp)
                            Text("Sin pedidos en esta categoría", fontSize = 14.sp, color = TextLight)
                        }
                    }
                    else -> LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.pedidosFiltrados, key = { it.id }) { pedido ->
                            AdminPedidoCard(
                                pedido = pedido,
                                onClick = { viewModel.onEvent(AdminEvent.VerDetalle(pedido)) },
                                onAvanzar = {
                                    pedido.estado.next()?.let { siguiente ->
                                        viewModel.onEvent(AdminEvent.CambiarEstado(pedido.id, siguiente))
                                    }
                                }
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }

    // ── Bottom sheet detalle ────────────────────────────────────────────────
    val pedidoActual = uiState.pedidoSeleccionado
    if (uiState.showDetalle && pedidoActual != null) {
        AdminDetalleSheet(
            pedido = pedidoActual,
            isActualizando = uiState.isActualizando,
            onDismiss = { viewModel.onEvent(AdminEvent.CerrarDetalle) },
            onCambiarEstado = { viewModel.onEvent(AdminEvent.CambiarEstado(pedidoActual.id, it)) }
        )
    }
}

// ── Tarjeta de pedido ────────────────────────────────────────────────────────
@Composable
private fun AdminPedidoCard(pedido: Pedido, onClick: () -> Unit, onAvanzar: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = Green.copy(alpha = 0.1f))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Franja de color según estado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Brush.horizontalGradient(listOf(pedido.estado.accentColor(), pedido.estado.accentColor().copy(alpha = 0.3f))))
            )
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Fila superior: número + hora
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(pedido.estado.emoji(), fontSize = 16.sp)
                        Text("Pedido #${pedido.id}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextDark)
                    }
                    Text(pedido.creadoEn.toHoraString(), fontSize = 12.sp, color = TextLight)
                }

                // Fila info + botón avanzar
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        pedido.cliente?.let {
                            Text("👤 ${it.nombre} ${it.apellido ?: ""}", fontSize = 13.sp, color = TextMid)
                        }
                        // Módulo badge
                        Box(
                            modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(GreenPale).padding(horizontal = 10.dp, vertical = 3.dp)
                        ) { Text(pedido.modulo.label(), fontSize = 11.sp, color = Green, fontWeight = FontWeight.SemiBold) }

                        // Resumen de productos
                        val resumen = buildString {
                            val items = pedido.detalles.mapNotNull { it.producto?.nombre }.plus(pedido.itemsLibres.map { it.descripcion })
                            append(items.take(2).joinToString(", "))
                            if (items.size > 2) append(" +${items.size - 2} más")
                        }
                        if (resumen.isNotBlank()) {
                            Text(resumen, fontSize = 12.sp, color = TextLight, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        if (!pedido.notas.isNullOrBlank()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = Color(0xFFE65100), modifier = Modifier.size(12.dp))
                                Text(pedido.notas, fontSize = 11.sp, color = Color(0xFFE65100), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Botón avanzar circular
                    pedido.estado.next()?.let {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(Green, GreenDark)))
                                .clickable { onAvanzar() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── Bottom Sheet de detalle ──────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminDetalleSheet(
    pedido: Pedido,
    isActualizando: Boolean,
    onDismiss: () -> Unit,
    onCambiarEstado: (EstadoPedido) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = {
            Box(
                modifier = Modifier.padding(top = 12.dp, bottom = 8.dp).size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp)).background(TextLight.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Pedido #${pedido.id}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(pedido.creadoEn.toFechaHoraString(), fontSize = 12.sp, color = TextLight)
                }
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(GreenPale).clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Default.Close, null, tint = Green, modifier = Modifier.size(18.dp)) }
            }

            // Estado actual
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(pedido.estado.accentColor().copy(alpha = 0.08f)).padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(pedido.estado.emoji(), fontSize = 24.sp)
                Column {
                    Text("Estado actual", fontSize = 11.sp, color = TextLight)
                    Text(pedido.estado.label(), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = pedido.estado.accentColor())
                }
            }

            HorizontalDivider(color = Color(0xFFCCE8E2))

            // Info cliente
            pedido.cliente?.let { c ->
                Text("Cliente", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMid)
                InfoRow("Nombre", "${c.nombre} ${c.apellido ?: ""}")
                c.telefono?.let { InfoRow("Teléfono", it) }
                c.email?.let { InfoRow("Email", it) }
                HorizontalDivider(color = Color(0xFFCCE8E2))
            }

            // Módulo y tipo
            Text("Detalles", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMid)
            InfoRow("Módulo", pedido.modulo.label())
            InfoRow("Tipo", if (pedido.tipo == TipoPedido.PARA_LLEVAR) "🛍 Para llevar" else "🏢 Oficina")

            // Productos
            if (pedido.detalles.isNotEmpty()) {
                HorizontalDivider(color = Color(0xFFCCE8E2))
                Text("Productos", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMid)
                pedido.detalles.forEach { d ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${d.cantidad}× ${d.producto?.nombre ?: "Producto #${d.productoId}"}", fontSize = 14.sp, color = TextDark, modifier = Modifier.weight(1f))
                        Text("$${"%.2f".format(d.subtotal)}", fontSize = 14.sp, color = Green, fontWeight = FontWeight.SemiBold)
                    }
                    d.notas?.let {
                        Text("  ↳ $it", fontSize = 12.sp, color = Color(0xFFE65100))
                    }
                }
            }

            // Items libres
            if (pedido.itemsLibres.isNotEmpty()) {
                HorizontalDivider(color = Color(0xFFCCE8E2))
                Text("Items especiales", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMid)
                pedido.itemsLibres.forEach { item ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.cantidad}× ${item.descripcion}", fontSize = 14.sp, color = TextDark, modifier = Modifier.weight(1f))
                        Text("$${"%.2f".format(item.subtotal)}", fontSize = 14.sp, color = Green, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Total
            val total = pedido.detalles.sumOf { it.subtotal } + pedido.itemsLibres.sumOf { it.subtotal }
            if (total > java.math.BigDecimal.ZERO) {
                HorizontalDivider(color = Color(0xFFCCE8E2))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextDark)
                    Text("$${"%.2f".format(total)}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Green)
                }
            }

            // Notas
            if (!pedido.notas.isNullOrBlank()) {
                HorizontalDivider(color = Color(0xFFCCE8E2))
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFFFF3E0)).padding(12.dp)
                ) { Text("📝 ${pedido.notas}", fontSize = 13.sp, color = Color(0xFFE65100)) }
            }

            HorizontalDivider(color = Color(0xFFCCE8E2))

            // Acciones de estado
            Text("Cambiar estado", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMid)

            if (isActualizando) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Green, modifier = Modifier.size(32.dp))
                }
            } else {
                // Siguiente estado
                pedido.estado.next()?.let { siguiente ->
                    Button(
                        onClick = { onCambiarEstado(siguiente) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        elevation = ButtonDefaults.buttonElevation(6.dp)
                    ) {
                        Text("${siguiente.emoji()} Marcar como ${siguiente.label()}", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                // Cancelar
                if (pedido.estado != EstadoPedido.CANCELADO && pedido.estado != EstadoPedido.ENTREGADO) {
                    OutlinedButton(
                        onClick = { onCambiarEstado(EstadoPedido.CANCELADO) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, ErrorRed.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        Text("❌ Cancelar pedido", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}

// ── Componentes pequeños ─────────────────────────────────────────────────────
@Composable
private fun AdminTopBtn(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(38.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp)) }
}

@Composable
private fun CounterBadge(emoji: String, label: String, count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.15f)).padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(emoji, fontSize = 16.sp)
            Text(count.toString(), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = TextLight)
        Text(value, fontSize = 13.sp, color = TextDark, fontWeight = FontWeight.Medium)
    }
}