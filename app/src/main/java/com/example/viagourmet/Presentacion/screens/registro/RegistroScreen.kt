package com.example.viagourmet.Presentacion.screens.registro

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.viagourmet.Presentacion.session.RolUsuario
import java.io.File

// Paleta CONALEP
private val Green       = Color(0xFF007E67)
private val GreenDark   = Color(0xFF005C4B)
private val GreenLight  = Color(0xFF00A882)
private val GreenPale   = Color(0xFFE6F4F1)
private val GreenMint   = Color(0xFFF0FAF7)
private val TextDark    = Color(0xFF0D2B24)
private val TextMid     = Color(0xFF4A7A6F)
private val TextLight   = Color(0xFF8AADA7)
private val InputBg     = Color(0xFFF2F9F7)
private val BorderLight = Color(0xFFCCE8E2)
private val ErrorColor  = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel = hiltViewModel(),
    onRegistroExitoso: (RolUsuario) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var nombre              by remember { mutableStateOf("") }
    var apellido            by remember { mutableStateOf("") }
    var telefono            by remember { mutableStateOf("") }
    var email               by remember { mutableStateOf("") }
    var password            by remember { mutableStateOf("") }
    var confirmarPassword   by remember { mutableStateOf("") }
    var isPasswordVisible   by remember { mutableStateOf(false) }
    var rolSeleccionado     by remember { mutableStateOf(RolUsuario.CLIENTE) }
    var fotoCredencialUri   by remember { mutableStateOf<Uri?>(null) }

    val tempFileUri = remember { createTempImageUri(context) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) fotoCredencialUri = tempFileUri
    }
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) cameraLauncher.launch(tempFileUri)
    }
    val galeriaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { fotoCredencialUri = it }
    }

    LaunchedEffect(uiState.registroExitoso) {
        if (uiState.registroExitoso) uiState.rolRegistrado?.let { onRegistroExitoso(it) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Green,
                        0.25f to GreenDark,
                        0.25f to GreenMint,
                        1.0f to GreenMint
                    )
                )
            )
    ) {
        // Círculos decorativos
        Box(
            modifier = Modifier.size(150.dp).offset(x = (-40).dp, y = (-40).dp)
                .clip(CircleShape).background(Color.White.copy(alpha = 0.07f))
        )
        Box(
            modifier = Modifier.size(90.dp).align(Alignment.TopEnd).offset(x = 20.dp, y = 30.dp)
                .clip(CircleShape).background(Color.White.copy(alpha = 0.06f))
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            // Header
            Box(
                modifier = Modifier.size(70.dp)
                    .shadow(16.dp, CircleShape).clip(CircleShape).background(Color.White),
                contentAlignment = Alignment.Center
            ) { Text("✍️", fontSize = 30.sp) }

            Spacer(Modifier.height(12.dp))
            Text("Crear cuenta", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("CONALEP · Cafetería", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f), letterSpacing = 1.5.sp)

            Spacer(Modifier.height(28.dp))

            // Tarjeta
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
                    .shadow(20.dp, RoundedCornerShape(28.dp), ambientColor = Green.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Información personal", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextMid, letterSpacing = 0.5.sp)

                    // Selector de rol
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                            .background(GreenPale).padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(RolUsuario.CLIENTE to "👤  Cliente", RolUsuario.EMPLEADO to "💼  Empleado").forEach { (rol, label) ->
                            val sel = rolSeleccionado == rol
                            Box(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(9.dp))
                                    .background(if (sel) Brush.horizontalGradient(listOf(Green, GreenLight)) else Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent)))
                                    .clickable { rolSeleccionado = rol }.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, fontSize = 13.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = if (sel) Color.White else TextMid)
                            }
                        }
                    }

                    // Foto credencial (solo cliente)
                    if (rolSeleccionado == RolUsuario.CLIENTE) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Foto de credencial", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMid)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(72.dp).clip(RoundedCornerShape(16.dp))
                                        .background(GreenPale).border(1.5.dp, BorderLight, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (fotoCredencialUri != null) {
                                        androidx.compose.foundation.Image(
                                            painter = rememberAsyncImagePainter(fotoCredencialUri),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(Icons.Default.Person, null, tint = Green, modifier = Modifier.size(30.dp))
                                    }
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    SmallGreenButton("📷 Cámara") { permisoCamara.launch(Manifest.permission.CAMERA) }
                                    SmallGreenButton("🖼 Galería") { galeriaLauncher.launch("image/*") }
                                }
                            }
                        }
                    }

                    // Campos
                    RegistroField("Nombre *", "Juan", nombre, { nombre = it }, Icons.Default.Person)
                    RegistroField("Apellido", "García", apellido, { apellido = it }, Icons.Default.Person)
                    RegistroField("Teléfono", "9611234567", telefono, { telefono = it }, Icons.Default.Phone, KeyboardType.Phone)
                    RegistroField("Correo electrónico *", "usuario@conalep.edu.mx", email, { email = it }, Icons.Default.Email, KeyboardType.Email)

                    // Contraseña
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Contraseña * (mín. 6 caracteres)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMid)
                        PasswordFieldRow(password, { password = it }, isPasswordVisible, "••••••••")
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Confirmar contraseña *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMid)
                        PasswordFieldRow(confirmarPassword, { confirmarPassword = it }, isPasswordVisible, "••••••••")
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isPasswordVisible, onCheckedChange = { isPasswordVisible = it },
                            colors = CheckboxDefaults.colors(checkedColor = Green, uncheckedColor = TextLight)
                        )
                        Text("Mostrar contraseña", fontSize = 13.sp, color = TextMid)
                    }

                    AnimatedVisibility(visible = uiState.errorMessage != null, enter = fadeIn(), exit = fadeOut()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                                .background(ErrorColor.copy(alpha = 0.08f)).padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 14.sp)
                            Text(uiState.errorMessage ?: "", color = ErrorColor, fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.onEvent(RegistroEvent.Registrar(
                                nombre = nombre, apellido = apellido.ifBlank { null },
                                telefono = telefono.ifBlank { null }, email = email,
                                password = password, confirmarPassword = confirmarPassword,
                                rol = rolSeleccionado, fotoCredencialUri = fotoCredencialUri?.toString()
                            ))
                        },
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        enabled = !uiState.isLoading && nombre.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green, disabledContainerColor = Green.copy(alpha = 0.35f)),
                        elevation = ButtonDefaults.buttonElevation(8.dp, 2.dp)
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                        else Text("Registrarme", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 0.5.sp)
                    }

                    TextButton(onClick = onNavigateToLogin, modifier = Modifier.fillMaxWidth()) {
                        Text("¿Ya tienes cuenta? Inicia sesión", fontSize = 13.sp, color = Green, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun RegistroField(
    label: String, placeholder: String, value: String, onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextMid)
        Row(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(InputBg).padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(icon, null, tint = Color(0xFF007E67), modifier = Modifier.size(16.dp))
            BasicTextField(
                value = value, onValueChange = onValueChange,
                modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                singleLine = true,
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

@Composable
private fun PasswordFieldRow(value: String, onValueChange: (String) -> Unit, visible: Boolean, placeholder: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(InputBg).padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Default.Lock, null, tint = Color(0xFF007E67), modifier = Modifier.size(16.dp))
        BasicTextField(
            value = value, onValueChange = onValueChange,
            modifier = Modifier.weight(1f).padding(vertical = 10.dp),
            singleLine = true,
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            textStyle = TextStyle(fontSize = 14.sp, color = TextDark),
            decorationBox = { inner ->
                if (value.isEmpty()) Text(placeholder, fontSize = 14.sp, color = TextLight)
                inner()
            }
        )
    }
}

@Composable
private fun SmallGreenButton(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFE6F4F1),
        border = BorderStroke(1.dp, Color(0xFF007E67).copy(alpha = 0.4f))
    ) {
        Text(text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, color = Color(0xFF007E67), fontWeight = FontWeight.Medium)
    }
}

private fun createTempImageUri(context: Context): Uri {
    val file = File(context.cacheDir, "credencial_temp_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}