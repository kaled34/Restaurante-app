package com.example.viagourmet.Presentacion.screens.perfil

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    onNavigateBack: () -> Unit,
    viewModel: PerfilViewModel = hiltViewModel()
) {
    val usuario by viewModel.usuario.collectAsState()
    val ubicacion by viewModel.ubicacion.collectAsState()
    val isFakeGps by viewModel.isFakeGpsDetected.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            viewModel.obtenerUbicacionActual()
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF007E67),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF0FAF7))
        ) {
            // BANNER DE ALERTA FAKE GPS
            AnimatedVisibility(visible = isFakeGps) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Color.White)
                        Text(
                            "ATENCIÓN: Se detectó el uso de ubicación simulada (Fake GPS). Por seguridad, algunas funciones están bloqueadas.",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // FOTO DE PERFIL
                Card(
                    shape = CircleShape,
                    elevation = CardDefaults.cardElevation(10.dp),
                    modifier = Modifier.size(150.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (usuario?.fotoUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(usuario?.fotoUri),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp),
                                tint = Color(0xFF007E67).copy(alpha = 0.2f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // INFORMACIÓN DEL CLIENTE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        PerfilInfoItem(label = "NOMBRE", value = "${usuario?.nombre} ${usuario?.apellido ?: ""}")
                        Divider(color = Color(0xFFF0FAF7))
                        PerfilInfoItem(label = "CORREO", value = usuario?.email ?: "")
                        Divider(color = Color(0xFFF0FAF7))
                        
                        // SECCIÓN DE UBICACIÓN GPS
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn, 
                                    contentDescription = null, 
                                    tint = if (isFakeGps) Color.Red else Color(0xFF007E67), 
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "UBICACIÓN ACTUAL (GPS)", 
                                    fontSize = 10.sp, 
                                    fontWeight = FontWeight.ExtraBold, 
                                    color = if (isFakeGps) Color.Red else Color(0xFF007E67).copy(alpha = 0.6f)
                                )
                            }
                            Text(
                                text = ubicacion, 
                                fontSize = 15.sp, 
                                fontWeight = FontWeight.SemiBold, 
                                color = if (isFakeGps) Color.Red else Color(0xFF0D2B24)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                
                Text(
                    text = if (isFakeGps) "ACCESO RESTRINGIDO" else "Ubicación verificada",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFakeGps) Color.Red else Color.Gray.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun PerfilInfoItem(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF007E67).copy(alpha = 0.6f))
        Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0D2B24))
    }
}
