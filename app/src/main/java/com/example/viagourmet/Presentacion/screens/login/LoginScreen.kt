package com.example.viagourmet.Presentacion.screens.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.viagourmet.Presentacion.session.RolUsuario

// ── Paleta oficial CONALEP ───────────────────────────────────────────────────
private val ConalepGreen        = Color(0xFF007E67)   // Verde PANTONE 335 C — color oficial
private val ConalepGreenDark    = Color(0xFF005C4B)   // Verde oscuro para detalles
private val ConalepGreenLight   = Color(0xFF00A882)   // Verde claro / hover
private val ConalepGreenPale    = Color(0xFFE6F4F1)   // Verde muy suave para fondos
private val ConalepGreenMint    = Color(0xFFF0FAF7)   // Fondo de pantalla principal

private val White               = Color(0xFFFFFFFF)
private val TextDark            = Color(0xFF0D2B24)   // Texto principal oscuro
private val TextMid             = Color(0xFF4A7A6F)   // Texto secundario verdoso
private val TextLight           = Color(0xFF8AADA7)   // Placeholders / labels sutiles
private val InputBg             = Color(0xFFF2F9F7)   // Fondo de inputs
private val BorderLight         = Color(0xFFCCE8E2)   // Borde suave
private val ErrorColor          = Color(0xFFD32F2F)
private val WarningYellow       = Color(0xFFFFF8E1)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: (RolUsuario) -> Unit,
    onNavigateToRegistro: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var email              by remember { mutableStateOf("") }
    var password           by remember { mutableStateOf("") }
    var isPasswordVisible  by remember { mutableStateOf(false) }
    var rolSeleccionado    by remember { mutableStateOf(RolUsuario.CLIENTE) }

    LaunchedEffect(uiState.loginExitoso) {
        if (uiState.loginExitoso) {
            uiState.rolLogueado?.let { onLoginSuccess(it) }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to ConalepGreen,
                        0.38f to ConalepGreenDark,
                        0.38f to ConalepGreenMint,
                        1.0f to ConalepGreenMint
                    )
                )
            )
    ) {
        // ── Círculos decorativos en la cabecera verde ───────────────────────
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = (-50).dp, y = (-50).dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.07f))
        )
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = 40.dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.06f))
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-40).dp, y = 150.dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.1f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── CABECERA VERDE ──────────────────────────────────────────────
            Spacer(Modifier.height(56.dp))

            // Logo circular CONALEP
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .shadow(20.dp, CircleShape, ambientColor = ConalepGreenDark.copy(alpha = 0.4f))
                    .clip(CircleShape)
                    .background(White),
                contentAlignment = Alignment.Center
            ) {
                // Icono institucional (simulado con texto/emoji)
                Text(
                    text = "🎓",
                    fontSize = 36.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = "La cafeteria",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                letterSpacing = (-0.3).sp
            )

            Spacer(Modifier.height(4.dp))



            Spacer(Modifier.height(36.dp))

            // ── TARJETA PRINCIPAL (sobre fondo blanco-verde) ────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .shadow(
                        elevation = 24.dp,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 28.dp, bottomEnd = 28.dp),
                        ambientColor = ConalepGreen.copy(alpha = 0.2f),
                        spotColor = ConalepGreen.copy(alpha = 0.3f)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {

                    // Título de la tarjeta
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Iniciar sesión",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Text(
                            text = "Ingresa tus credenciales para continuar",
                            fontSize = 13.sp,
                            color = TextLight
                        )
                    }

                    // ── Selector de rol ─────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(ConalepGreenPale)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(
                            RolUsuario.CLIENTE  to "Cliente",
                            RolUsuario.EMPLEADO to "Empleado"
                        ).forEach { (rol, label) ->
                            val isSelected = rolSeleccionado == rol
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(
                                        if (isSelected)
                                            Brush.horizontalGradient(
                                                listOf(ConalepGreen, ConalepGreenLight)
                                            )
                                        else
                                            Brush.horizontalGradient(
                                                listOf(Color.Transparent, Color.Transparent)
                                            )
                                    )
                                    .clickable { rolSeleccionado = rol }
                                    .padding(vertical = 11.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) White else TextMid
                                )
                            }
                        }
                    }

                    // ── Campo Email ─────────────────────────────────────────
                    ConceptField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Correo electrónico",
                        placeholder = "usuario@conalep.edu.mx",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = ConalepGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    // ── Campo Contraseña ────────────────────────────────────
                    ConceptField(
                        value = password,
                        onValueChange = { password = it },
                        label = "Contraseña",
                        placeholder = "••••••••",
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = ConalepGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            IconButton(
                                onClick = { isPasswordVisible = !isPasswordVisible },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    if (isPasswordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    // ── Mensaje de error ────────────────────────────────────
                    AnimatedVisibility(
                        visible = uiState.errorMessage != null,
                        enter = fadeIn() + slideInVertically(),
                        exit  = fadeOut()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(ErrorColor.copy(alpha = 0.08f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 14.sp)
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = ErrorColor,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // ── Botón principal CONALEP ─────────────────────────────
                    Button(
                        onClick = { viewModel.login(email, password, rolSeleccionado) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        enabled = email.isNotBlank() && password.isNotBlank() && !uiState.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ConalepGreen,
                            disabledContainerColor = ConalepGreen.copy(alpha = 0.35f)
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 8.dp,
                            pressedElevation  = 2.dp,
                            disabledElevation = 0.dp
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier  = Modifier.size(22.dp),
                                color     = White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text       = "Entrar",
                                fontSize   = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color      = White,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // ── Divider + Registro ──────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = BorderLight
                        )
                        Text(
                            "  o  ",
                            fontSize = 12.sp,
                            color = TextLight
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = BorderLight
                        )
                    }

                    OutlinedButton(
                        onClick  = onNavigateToRegistro,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape  = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp, ConalepGreen.copy(alpha = 0.6f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ConalepGreen
                        )
                    ) {
                        Text(
                            "Crear cuenta nueva",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Pie institucional ───────────────────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text      = "CONALEP",
                    fontSize  = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color     = TextMid,
                    letterSpacing = 2.sp
                )
                Text(
                    text     = "Educación de Calidad para la Competitividad",
                    fontSize = 10.sp,
                    color    = TextLight,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

// ── Campo de texto estilo CONALEP ────────────────────────────────────────────
@Composable
private fun ConceptField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text       = label,
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color      = TextMid,
            letterSpacing = 0.2.sp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(InputBg)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            leadingIcon?.invoke()

            BasicTextField(
                value             = value,
                onValueChange     = onValueChange,
                modifier          = Modifier
                    .weight(1f)
                    .padding(vertical = 10.dp),
                singleLine        = true,
                visualTransformation = visualTransformation,
                keyboardOptions   = keyboardOptions,
                textStyle         = TextStyle(
                    fontSize   = 14.sp,
                    color      = TextDark,
                    fontWeight = FontWeight.Normal
                ),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text     = placeholder,
                            fontSize = 14.sp,
                            color    = TextLight
                        )
                    }
                    innerTextField()
                }
            )
            trailingIcon?.invoke()
        }
    }
}