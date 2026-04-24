package com.example.viagourmet

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.viagourmet.Presentacion.navegacion.NavegacionGraph
import com.example.viagourmet.Presentacion.session.SessionManager
import com.example.viagourmet.Presentacion.theme.ViaGourmetTheme
import com.example.viagourmet.data.repository.AuthRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Obtener token inicial
        obtenerTokenFCM()

        setContent {
            ViaGourmetTheme {
                // Solicitar permiso de notificaciones en Android 13+
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        Log.d("MainActivity", "Permiso de notificaciones concedido")
                    }
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavegacionGraph(sessionManager = sessionManager)
                }
            }
        }
    }

    private fun obtenerTokenFCM() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("MainActivity", "Error obteniendo token FCM", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("MainActivity", "Token FCM actual: $token")
            
            // Vincular token si el usuario ya está logueado
            lifecycleScope.launch {
                authRepository.vincularTokenFcm(token)
            }
        }
    }
}