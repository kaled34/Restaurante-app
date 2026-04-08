package com.example.viagourmet.Presentacion.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.viagourmet.domain.model.Producto

// ── Paleta CONALEP ────────────────────────────────────────────────────────────
private val Green       = Color(0xFF007E67)
private val GreenDark   = Color(0xFF005C4B)
private val GreenLight  = Color(0xFF00A882)
private val GreenPale   = Color(0xFFE6F4F1)
private val GreenMint   = Color(0xFFF0FAF7)
private val TextDark    = Color(0xFF0D2B24)
private val TextMid     = Color(0xFF4A7A6F)
private val TextLight   = Color(0xFF8AADA7)
private val InputBg     = Color(0xFFF2F9F7)
private val CardBg      = Color(0xFFFFFFFF)
private val ErrorRed    = Color(0xFFD32F2F)
private val WarnOrange  = Color(0xFFE65100)
private val BorderSoft  = Color(0xFFCCE8E2)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarMenuScreen(
    viewModel: EditarMenuViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensajeExito) {
        uiState.mensajeExito?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(EditarMenuEvent.LimpiarMensaje)
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
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .clickable { onNavigateBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Regresar",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                "Editar Menú",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "${uiState.totalActivos} activos · ${uiState.totalInactivos} inactivos",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                    // Botón agregar
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { viewModel.onEvent(EditarMenuEvent.AbrirNuevoProducto) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar producto",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onEvent(EditarMenuEvent.AbrirNuevoProducto) },
                containerColor = Green,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Text("Agregar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Barra de búsqueda ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(GreenDark, Green)))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                    BasicTextField(
                        value = uiState.filtroBusqueda,
                        onValueChange = { viewModel.onEvent(EditarMenuEvent.BuscarProducto(it)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 14.sp, color = Color.White),
                        decorationBox = { inner ->
                            if (uiState.filtroBusqueda.isEmpty())
                                Text(
                                    "Buscar producto...",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            inner()
                        }
                    )
                    if (uiState.filtroBusqueda.isNotEmpty()) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Limpiar",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { viewModel.onEvent(EditarMenuEvent.BuscarProducto("")) }
                        )
                    }
                }
            }

            // ── Chips de categoría ───────────────────────────────────────────
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FiltroChip(
                        label = "Todos",
                        count = uiState.productos.size,
                        selected = uiState.filtroCategoria == null,
                        onClick = { viewModel.onEvent(EditarMenuEvent.FiltrarCategoria(null)) }
                    )
                }
                items(uiState.categorias) { cat ->
                    FiltroChip(
                        label = cat.nombre,
                        count = uiState.productos.count { it.categoriaId == cat.id },
                        selected = uiState.filtroCategoria == cat.id,
                        onClick = { viewModel.onEvent(EditarMenuEvent.FiltrarCategoria(cat.id)) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Lista de productos ───────────────────────────────────────────
            if (uiState.productosFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🍽", fontSize = 48.sp)
                        Text(
                            "Sin productos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            "Agrega el primer producto con el botón +",
                            fontSize = 13.sp,
                            color = TextLight
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 88.dp  // espacio para el FAB
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.productosFiltrados, key = { it.id }) { producto ->
                        ProductoAdminCard(
                            producto  = producto,
                            onEditar  = { viewModel.onEvent(EditarMenuEvent.AbrirEditarProducto(producto)) },
                            onToggle  = { viewModel.onEvent(EditarMenuEvent.ToggleDisponibilidad(producto.id)) },
                            onEliminar = { viewModel.onEvent(EditarMenuEvent.EliminarProducto(producto.id)) }
                        )
                    }
                }
            }
        }
    }

    // ── Bottom Sheet: formulario ─────────────────────────────────────────────
    if (uiState.showFormulario) {
        FormularioProductoSheet(
            isEditing = uiState.isEditing,
            productoExistente = uiState.productoSeleccionado,
            categorias = uiState.categorias,
            errorMessage = uiState.errorMessage,
            onDismiss = { viewModel.onEvent(EditarMenuEvent.CerrarFormulario) },
            onGuardar = { form -> viewModel.onEvent(EditarMenuEvent.GuardarProducto(form)) }
        )
    }
}

// ── Tarjeta de producto para admin ──────────────────────────────────────────

@Composable
private fun ProductoAdminCard(
    producto: Producto,
    onEditar: () -> Unit,
    onToggle: () -> Unit,
    onEliminar: () -> Unit
) {
    var showConfirmEliminar by remember { mutableStateOf(false) }

    if (showConfirmEliminar) {
        AlertDialog(
            onDismissRequest = { showConfirmEliminar = false },
            containerColor = CardBg,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    "Eliminar producto",
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
            },
            text = {
                Text(
                    "¿Eliminar \"${producto.nombre}\"? Esta acción no se puede deshacer.",
                    color = TextMid
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmEliminar = false
                    onEliminar()
                }) {
                    Text("Eliminar", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmEliminar = false }) {
                    Text("Cancelar", color = Green)
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                4.dp,
                RoundedCornerShape(16.dp),
                ambientColor = Green.copy(alpha = 0.07f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (producto.disponible) CardBg else Color(0xFFF5F5F5)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Franja de estado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        if (producto.disponible)
                            Brush.horizontalGradient(listOf(Green, GreenLight))
                        else
                            Brush.horizontalGradient(listOf(TextLight, TextLight.copy(alpha = 0.3f)))
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji / imagen placeholder
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (producto.disponible) GreenPale else Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = productoEmoji(producto.nombre),
                        fontSize = 26.sp
                    )
                }

                // Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = producto.nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (producto.disponible) TextDark else TextLight,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$${"%.2f".format(producto.precio)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (producto.disponible) Green else TextLight
                        )
                    }

                    Text(
                        text = producto.descripcion,
                        fontSize = 12.sp,
                        color = TextLight,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        producto.categoria?.let { cat ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GreenPale)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    cat.nombre,
                                    fontSize = 10.sp,
                                    color = Green,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (producto.disponible)
                                        Color(0xFFE8F5E9)
                                    else
                                        Color(0xFFFFEBEE)
                                )
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (producto.disponible) "Disponible" else "No disponible",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (producto.disponible) Color(0xFF2E7D32) else ErrorRed
                            )
                        }
                    }
                }
            }

            // Botones de acción
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Editar
                OutlinedButton(
                    onClick = onEditar,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Green.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Green),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Editar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                // Disponibilidad
                Button(
                    onClick = onToggle,
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (producto.disponible)
                            Color(0xFFFF6F00).copy(alpha = 0.15f)
                        else
                            Color(0xFF2E7D32).copy(alpha = 0.15f),
                        contentColor = if (producto.disponible)
                            WarnOrange
                        else
                            Color(0xFF2E7D32)
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(
                        if (producto.disponible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (producto.disponible) "Ocultar" else "Activar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Eliminar
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ErrorRed.copy(alpha = 0.1f))
                        .clickable { showConfirmEliminar = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = ErrorRed,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

// ── Chip de filtro ────────────────────────────────────────────────────────────

@Composable
private fun FiltroChip(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) Green else CardBg)
            .then(
                if (!selected) Modifier.shadow(0.dp)
                else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else TextMid
        )
        if (count > 0) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) Color.White.copy(alpha = 0.3f) else GreenPale
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    count.toString(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.White else Green
                )
            }
        }
    }
}

// ── Bottom Sheet: formulario de producto ─────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormularioProductoSheet(
    isEditing: Boolean,
    productoExistente: Producto?,
    categorias: List<com.example.viagourmet.domain.model.Categoria>,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onGuardar: (FormularioProducto) -> Unit
) {
    // Prellenar el formulario si estamos editando
    var nombre      by remember { mutableStateOf(productoExistente?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(productoExistente?.descripcion ?: "") }
    var precio      by remember { mutableStateOf(productoExistente?.precio?.toPlainString() ?: "") }
    var catId       by remember { mutableIntStateOf(productoExistente?.categoriaId ?: categorias.firstOrNull()?.id ?: 1) }
    var disponible  by remember { mutableStateOf(productoExistente?.disponible ?: true) }
    var imagenUrl   by remember { mutableStateOf(productoExistente?.imagenUrl ?: "") }

    val formularioValido = nombre.isNotBlank() &&
            descripcion.isNotBlank() &&
            (precio.toBigDecimalOrNull()?.signum() ?: 0) > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TextLight.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isEditing) "Editar producto" else "Nuevo producto",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = if (isEditing) "Modifica los campos necesarios" else "Completa la información",
                        fontSize = 12.sp,
                        color = TextLight
                    )
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(GreenPale)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Close, null, tint = Green, modifier = Modifier.size(17.dp))
                }
            }

            HorizontalDivider(color = BorderSoft)

            // Campo Nombre
            FormField(
                label = "Nombre del producto *",
                value = nombre,
                placeholder = "Ej: Café Americano",
                onValueChange = { nombre = it }
            )

            // Campo Descripción
            FormField(
                label = "Descripción *",
                value = descripcion,
                placeholder = "Ej: Café recién hecho, aroma intenso",
                onValueChange = { descripcion = it },
                singleLine = false
            )

            // Precio
            FormField(
                label = "Precio *",
                value = precio,
                placeholder = "Ej: 45.00",
                onValueChange = { precio = it },
                keyboardType = KeyboardType.Decimal,
                prefix = "$"
            )

            // Selector de categoría
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Categoría *",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMid
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categorias) { cat ->
                        val sel = catId == cat.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (sel) Green else GreenPale)
                                .clickable { catId = cat.id }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                cat.nombre,
                                fontSize = 12.sp,
                                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                                color = if (sel) Color.White else TextMid
                            )
                        }
                    }
                }
            }

            // Disponibilidad
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (disponible) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (disponible) "✅" else "❌", fontSize = 18.sp)
                    Column {
                        Text(
                            "Disponible para venta",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (disponible) Color(0xFF2E7D32) else ErrorRed
                        )
                        Text(
                            if (disponible) "Los clientes pueden pedirlo" else "Oculto en el menú",
                            fontSize = 11.sp,
                            color = TextLight
                        )
                    }
                }
                Switch(
                    checked = disponible,
                    onCheckedChange = { disponible = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Green,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = ErrorRed.copy(alpha = 0.5f)
                    )
                )
            }

            // Error
            AnimatedVisibility(
                visible = errorMessage != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(ErrorRed.copy(alpha = 0.08f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠️", fontSize = 14.sp)
                    Text(errorMessage ?: "", fontSize = 12.sp, color = ErrorRed)
                }
            }

            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderSoft),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMid)
                ) {
                    Text("Cancelar", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = {
                        onGuardar(
                            FormularioProducto(
                                nombre      = nombre,
                                descripcion = descripcion,
                                precioTexto = precio,
                                categoriaId = catId,
                                disponible  = disponible,
                                imagenUrl   = imagenUrl
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    enabled = formularioValido,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green,
                        disabledContainerColor = Green.copy(alpha = 0.35f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(6.dp)
                ) {
                    Icon(
                        if (isEditing) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isEditing) "Guardar cambios" else "Agregar",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Campo de formulario reutilizable ─────────────────────────────────────────

@Composable
private fun FormField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    prefix: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMid
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(InputBg)
                .padding(horizontal = 14.dp, vertical = if (singleLine) 0.dp else 4.dp),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (prefix != null) {
                Text(
                    prefix,
                    fontSize = 14.sp,
                    color = Green,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = if (singleLine) 14.dp else 14.dp)
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = if (singleLine) 14.dp else 10.dp),
                singleLine = singleLine,
                maxLines = if (singleLine) 1 else 3,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                textStyle = TextStyle(fontSize = 14.sp, color = TextDark),
                decorationBox = { inner ->
                    if (value.isEmpty()) Text(placeholder, fontSize = 14.sp, color = TextLight)
                    inner()
                }
            )
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun productoEmoji(nombre: String): String = when {
    nombre.contains("café", ignoreCase = true) -> "☕"
    nombre.contains("jugo", ignoreCase = true) -> "🥤"
    nombre.contains("hot cake", ignoreCase = true) ||
            nombre.contains("hotcake", ignoreCase = true) -> "🥞"
    nombre.contains("pollo", ignoreCase = true) ||
            nombre.contains("pechuga", ignoreCase = true) -> "🍗"
    nombre.contains("pasta", ignoreCase = true) -> "🍝"
    nombre.contains("hambur", ignoreCase = true) -> "🍔"
    nombre.contains("chilaquil", ignoreCase = true) ||
            nombre.contains("huevo", ignoreCase = true) ||
            nombre.contains("enfrijol", ignoreCase = true) -> "🍳"
    nombre.contains("frijol", ignoreCase = true) -> "🫘"
    nombre.contains("arroz", ignoreCase = true) -> "🍚"
    nombre.contains("menú", ignoreCase = true) ||
            nombre.contains("menu", ignoreCase = true) -> "🍱"
    else -> "🍽"
}