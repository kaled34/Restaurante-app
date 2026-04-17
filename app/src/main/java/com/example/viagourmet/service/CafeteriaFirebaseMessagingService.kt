package com.example.viagourmet.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.viagourmet.MainActivity
import com.example.viagourmet.Presentacion.session.SessionManager
import com.example.viagourmet.data.api.CafeteriaApiService
import com.example.viagourmet.data.model.request.FcmTokenRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CafeteriaFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var apiService: CafeteriaApiService
    @Inject lateinit var sessionManager: SessionManager

    companion object {
        const val CHANNEL_ID   = "cafeteria_pedidos"
        const val CHANNEL_NAME = "Pedidos Cafetería"
    }

    /** Se llama cuando llega un nuevo token FCM (primera vez o al rotar) */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Guardar en SharedPreferences
        getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
            .edit().putString("fcm_token", token).apply()

        // Enviar al servidor si hay sesión activa
        val sesion = sessionManager.obtenerSesion() ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                apiService.actualizarFcmToken(sesion.id, FcmTokenRequest(token))
            } catch (e: Exception) {
                // No bloquear en caso de falla de red
            }
        }
    }

    /** Se llama cuando llega una notificación con la app en primer plano */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "Cafetería"
        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "Tienes una actualización"

        mostrarNotificacion(title, body)
    }

    private fun mostrarNotificacion(title: String, body: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notificaciones de pedidos de la cafetería" }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
