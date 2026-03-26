package com.example.viagourmet.Presentacion.screens.registro

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.example.viagourmet.Presentacion.session.RolUsuario
import com.example.viagourmet.Presentacion.theme.Brown80
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    viewModel: RegistroViewModel = hiltViewModel(),
    onRegistroExitoso: (RolUsuario) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var nombre by remember { mutableStateOf("") }
    var apellido by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rolSeleccionado by remember { mutableStateOf(RolUsuario.CLIENTE) }
    var fotoCredencialUri by remember { mutableStateOf<Uri?>(null) }

    // CameraX para tomar foto de credencial
    val tempFileUri = remember { createTempImageUri(context) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) fotoCredencialUri = tempFileUri
    }
    val permisoCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) cameraLauncher.launch(tempFileUri)
    }
    // También permitir seleccionar de galería
    val galeriaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { fotoCredencialUri = it } }

    LaunchedEffect(uiState.registroExitoso) {
        if (uiState.registroExitoso) {
            uiState.rolRegistrado?.let { onRegistroExitoso(it) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Cafetería", style = MaterialTheme.typography.headlineLarge, color = Brown80)
        Text("Crea tu cuenta", style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))

        // ── Foto de credencial (solo para CLIENTES) ──
        if (rolSeleccionado == RolUsuario.CLIENTE) {
            Text("Foto de credencial", style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, Brown80, RoundedCornerShape(8.dp))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                if (fotoCredencialUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(fotoCredencialUri),
                        contentDescription = "Foto credencial",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = null,
                        modifier = Modifier.size(48.dp), tint = Brown80)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { permisoCamara.launch(Manifest.permission.CAMERA) }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Cámara")
                }
                OutlinedButton(onClick = { galeriaLauncher.launch("image/*") }) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Galería")
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(value = nombre, onValueChange = { nombre = it },
            label = { Text("Nombre *") }, leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(value = apellido, onValueChange = { apellido = it },
            label = { Text("Apellido") }, leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(value = telefono, onValueChange = { telefono = it },
            label = { Text("Teléfono") }, leadingIcon = { Icon(Icons.Default.Phone, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(8.dp))

        Text("Tipo de cuenta", style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = rolSeleccionado == RolUsuario.CLIENTE,
                onClick = { rolSeleccionado = RolUsuario.CLIENTE },
                label = { Text("Cliente") }, modifier = Modifier.weight(1f))
            FilterChip(selected = rolSeleccionado == RolUsuario.EMPLEADO,
                onClick = { rolSeleccionado = RolUsuario.EMPLEADO },
                label = { Text("Empleado") }, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(value = email, onValueChange = { email = it },
            label = { Text("Email *") }, leadingIcon = { Icon(Icons.Default.Email, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(value = password, onValueChange = { password = it },
            label = { Text("Contraseña * (mín. 6 caracteres)") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(value = confirmarPassword, onValueChange = { confirmarPassword = it },
            label = { Text("Confirmar contraseña *") }, leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(), singleLine = true)

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isPasswordVisible, onCheckedChange = { isPasswordVisible = it })
            Text("Mostrar contraseña", style = MaterialTheme.typography.bodyMedium)
        }

        uiState.errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
        }
        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                viewModel.onEvent(RegistroEvent.Registrar(
                    nombre = nombre, apellido = apellido.ifBlank { null },
                    telefono = telefono.ifBlank { null }, email = email,
                    password = password, confirmarPassword = confirmarPassword,
                    rol = rolSeleccionado,
                    fotoCredencialUri = fotoCredencialUri?.toString()
                ))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading && nombre.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Brown80)
        ) {
            if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
            else Text("Registrarse")
        }

        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onNavigateToLogin) { Text("¿Ya tienes cuenta? Inicia sesión") }
    }
}

private fun createTempImageUri(context: Context): Uri {
    val file = File(context.cacheDir, "credencial_temp_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}
