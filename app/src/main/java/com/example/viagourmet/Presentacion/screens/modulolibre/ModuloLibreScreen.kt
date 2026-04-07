package com.example.viagourmet.Presentacion.screens.modulolibre

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

private val Green       = Color(0xFF007E67)
private val GreenDark   = Color(0xFF005C4B)
private val GreenPale   = Color(0xFFE6F4F1)
private val GreenMint   = Color(0xFFF0FAF7)
private val TextDark    = Color(0xFF0D2B24)
private val TextMid     = Color(0xFF4A7A6F)
private val TextLight   = Color(0xFF8AADA7)
private val InputBg     = Color(0xFFF2F9F7)
private val CardBg      = Color(0xFFFFFFFF)
private val ErrorRed    = Color(0xFFD32F2F)

val opcionesTiempo = listOf(25 to "25 minutos", 30 to "30 minutos", 35 to "35 minutos", 45 to "45 minutos", 60 to "1 hora")

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
        containerColor = GreenMint,
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
                        Text("✏️ Pedido Libre", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Describe lo que deseas", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Descripción
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tu pedido", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Text(
                        "Escribe con detalle. Ej: '2 tortas de jamón sin chile, 1 café americano grande'",
                        fontSize = 12.sp, color = TextLight
                    )
                    OutlinedTextField(
                        value = uiState.descripcion,
                        onValueChange = { viewModel.onDescripcionChange(it) },
                        modifier = Modifier.fillMaxWidth().height(130.dp),
                        placeholder = { Text("Escribe aquí tu pedido...", color = TextLight) },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Green,
                            unfocusedBorderColor = Color(0xFFCCE8E2),
                            focusedContainerColor = InputBg,
                            unfocusedContainerColor = InputBg
                        ),
                        maxLines = 5
                    )
                }
            }

            // Tiempo de entrega
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("¿Cuándo lo quieres?", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    opcionesTiempo.forEach { (minutos, etiqueta) ->
                        val isSelected = uiState.minutosSeleccionados == minutos
                        val urgenciaColor = when {
                            minutos <= 35 -> Color(0xFFE53935)
                            minutos == 45 -> Color(0xFFFDD835)
                            else -> Color(0xFF43A047)
                        }
                        val urgenciaLabel = when {
                            minutos <= 35 -> "🔴 Urgente"
                            minutos == 45 -> "🟡 Prioridad media"
                            else -> "🟢 Sin prisa"
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.onTiempoSeleccionado(minutos) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) GreenPale else Color(0xFFFAFAFA)),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Green) else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(urgenciaColor))
                                    Column {
                                        Text(etiqueta, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Green else TextDark)
                                        Text(urgenciaLabel, fontSize = 11.sp, color = TextLight)
                                    }
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.onTiempoSeleccionado(minutos) },
                                    colors = RadioButtonDefaults.colors(selectedColor = Green)
                                )
                            }
                        }
                    }
                }
            }

            // Método de pago
            Card(
                modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Checkbox(
                        checked = uiState.cuentaPendiente,
                        onCheckedChange = { viewModel.onCuentaPendienteChange(it) },
                        colors = CheckboxDefaults.colors(checkedColor = Green)
                    )
                    Column {
                        Text("Cuenta pendiente", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                        Text("Pagar después en caja", fontSize = 12.sp, color = TextLight)
                    }
                }
            }

            uiState.errorMessage?.let {
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(ErrorRed.copy(alpha = 0.08f)).padding(12.dp)
                ) {
                    Text("⚠️ $it", color = ErrorRed, fontSize = 13.sp)
                }
            }

            Button(
                onClick = { viewModel.enviarPedido() },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = !uiState.isLoading && uiState.descripcion.isNotBlank() && uiState.minutosSeleccionados != null,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green, disabledContainerColor = Green.copy(alpha = 0.35f)),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                if (uiState.isLoading) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                else Text("Enviar pedido libre", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}