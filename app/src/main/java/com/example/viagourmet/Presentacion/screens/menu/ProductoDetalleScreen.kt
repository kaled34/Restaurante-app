package com.example.viagourmet.Presentacion.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import coil.compose.AsyncImage
import com.example.viagourmet.Presentacion.components.CantidadSelector
import com.example.viagourmet.data.mock.MockData
import com.example.viagourmet.domain.model.Producto

// Paleta CONALEP — igual que el resto de la app
private val Green      = Color(0xFF007E67)
private val GreenDark  = Color(0xFF005C4B)
private val GreenLight = Color(0xFF00A882)
private val GreenPale  = Color(0xFFE6F4F1)
private val GreenMint  = Color(0xFFF0FAF7)
private val TextDark   = Color(0xFF0D2B24)
private val TextMid    = Color(0xFF4A7A6F)
private val TextLight  = Color(0xFF8AADA7)
private val CardBg     = Color(0xFFFFFFFF)
private val ErrorRed   = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoDetalleScreen(
    productoId: Int,
    onNavigateBack: () -> Unit,
    onAgregarAlPedido: (Producto, Int) -> Unit
) {
    val producto = remember(productoId) {
        MockData.productos.find { it.id == productoId }
    }

    var cantidad by remember { mutableIntStateOf(1) }

    if (producto == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GreenMint),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Text("🍽", fontSize = 56.sp)
                Text(
                    "Producto no encontrado",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Button(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green)
                ) {
                    Text("Regresar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val subtotal = producto.precio.multiply(java.math.BigDecimal(cantidad))

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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                            producto.nombre,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Text(
                            "Detalle del producto",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(CardBg)
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Selector de cantidad
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Cantidad",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMid
                        )
                        CantidadSelector(
                            cantidad = cantidad,
                            onCantidadChange = { cantidad = it },
                            minCantidad = 1,
                            maxCantidad = 10
                        )
                    }

                    // Línea divisora
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFCCE8E2))
                    )

                    // Total y botón
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total", fontSize = 12.sp, color = TextLight)
                            Text(
                                "$${"%.2f".format(subtotal)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Green
                            )
                        }

                        Button(
                            onClick = {
                                onAgregarAlPedido(producto, cantidad)
                                onNavigateBack()
                            },
                            enabled = producto.disponible,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Green,
                                disabledContainerColor = Green.copy(alpha = 0.35f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(8.dp, 2.dp)
                        ) {
                            Text(
                                if (producto.disponible) "🛒  Agregar al pedido" else "No disponible",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Imagen del producto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(GreenPale),
                contentAlignment = Alignment.Center
            ) {
                if (producto.imagenUrl != null) {
                    AsyncImage(
                        model = producto.imagenUrl,
                        contentDescription = producto.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Emoji grande como placeholder
                    Text(
                        text = when {
                            producto.nombre.contains("café", true) -> "☕"
                            producto.nombre.contains("jugo", true) -> "🥤"
                            producto.nombre.contains("hot", true)  -> "🥞"
                            producto.nombre.contains("pollo", true) || producto.nombre.contains("pechuga", true) -> "🍗"
                            producto.nombre.contains("pasta", true) -> "🍝"
                            producto.nombre.contains("hambur", true) -> "🍔"
                            producto.nombre.contains("chilaquil", true) -> "🍳"
                            producto.nombre.contains("frijol", true) -> "🫘"
                            producto.nombre.contains("arroz", true) -> "🍚"
                            producto.nombre.contains("huevo", true) -> "🍳"
                            else -> "🍽"
                        },
                        fontSize = 80.sp
                    )
                }

                // Badge de no disponible
                if (!producto.disponible) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ErrorRed)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Agotado", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Contenido principal
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Nombre y precio
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = Green.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                producto.nombre,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextDark,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "$${"%.2f".format(producto.precio)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Green
                            )
                        }

                        // Categoría badge
                        producto.categoria?.let { cat ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(GreenPale)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    cat.nombre,
                                    fontSize = 11.sp,
                                    color = Green,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Descripción
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(18.dp), ambientColor = Green.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Descripción",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMid
                        )
                        Text(
                            producto.descripcion,
                            fontSize = 14.sp,
                            color = TextDark,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Aviso si no está disponible
                if (!producto.disponible) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(ErrorRed.copy(alpha = 0.08f))
                            .padding(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("❌", fontSize = 18.sp)
                            Text(
                                "Este producto no está disponible temporalmente.",
                                fontSize = 13.sp,
                                color = ErrorRed,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}