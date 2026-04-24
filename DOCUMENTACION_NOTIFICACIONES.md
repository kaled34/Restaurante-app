# Documentación de Implementación: Notificaciones Push (FCM) - ViaGourmet

Esta documentación detalla la integración técnica de Firebase Cloud Messaging (FCM) en la aplicación **ViaGourmet**. Se describe la arquitectura, los cambios realizados y los fragmentos de código clave para su mantenimiento y escalabilidad.

---

## 1. Importancia de las Notificaciones Push
En una plataforma de servicios como **ViaGourmet**, la comunicación asíncrona es vital:
*   **Gestión de Pedidos**: Notificar al cliente cuando su pedido cambia de "Pendiente" a "En Camino".
*   **Alertas de Administración**: Los administradores reciben avisos inmediatos sobre nuevos pedidos realizados, optimizando los tiempos de respuesta.
*   **Engagement**: Envío de ofertas personalizadas basadas en el comportamiento del usuario.

## 2. Configuración y Dependencias

### A. Configuración de Gradle
Se utiliza el Firebase BOM para asegurar la compatibilidad de versiones. En `app/build.gradle.kts`:

```kotlin
dependencies {
    // Firebase BOM — gestiona versiones automáticamente
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")
}
```

### B. Manifiesto de la Aplicación
Para que el sistema reconozca el servicio de Firebase y maneje los permisos, se realizaron los siguientes ajustes en `AndroidManifest.xml`:

```xml
<!-- Permiso para notificaciones (Android 13+) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<application ...>
    <!-- Firebase Cloud Messaging Service -->
    <service
        android:name=".service.CafeteriaFirebaseMessagingService"
        android:exported="false">
        <intent-filter>
            <action android:name="com.google.firebase.MESSAGING_EVENT" />
        </intent-filter>
    </service>
</application>
```

---

## 3. Implementación del Servicio de Mensajería

El archivo `CafeteriaFirebaseMessagingService.kt` es el núcleo de la recepción de mensajes. Implementa la lógica para capturar tokens y mostrar notificaciones visuales.

### A. Gestión del Token FCM
Cuando se genera un nuevo token (por instalación o expiración), se vincula con el usuario actual:

```kotlin
override fun onNewToken(token: String) {
    super.onNewToken(token)
    Log.d("FCM", "Nuevo Token: $token")
    
    // Vincular el token con el usuario si hay una sesión activa
    scope.launch {
        authRepository.vincularTokenFcm(token)
    }
}
```

### B. Procesamiento de Mensajes Recibidos
Se diferencia el comportamiento según el estado de la aplicación:

```kotlin
override fun onMessageReceived(message: RemoteMessage) {
    super.onMessageReceived(message)
    
    // Procesar notificación cuando la app está en primer plano
    message.notification?.let {
        mostrarNotificacion(it.title ?: "ViaGourmet", it.body ?: "")
    }
}
```

### C. Construcción de la Notificación (Notification Channel)
Desde Android 8.0, es obligatorio el uso de canales. El ID utilizado es `pedidos_channel`.

```kotlin
private fun mostrarNotificacion(titulo: String, mensaje: String) {
    val channelId = "pedidos_channel"
    val notificationBuilder = NotificationCompat.Builder(this, channelId)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(titulo)
        .setContentText(mensaje)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)

    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId, "Notificaciones de Pedidos",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }
    notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
}
```

---

## 4. Lógica en la Actividad Principal (`MainActivity`)

### A. Solicitud de Permisos en Runtime
Para cumplir con las políticas de privacidad de Android 13+, se solicita permiso explícito al usuario:

```kotlin
val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) Log.d("MainActivity", "Permiso concedido")
}

LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) 
            != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
```

### B. Recuperación de Token al Iniciar
Se asegura que el servidor tenga siempre el token más reciente al abrir la app:

```kotlin
private fun obtenerTokenFCM() {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val token = task.result
            Log.d("FCM_DIAG", "TOKEN FCM: $token")
            lifecycleScope.launch {
                authRepository.vincularTokenFcm(token)
            }
        }
    }
}
```

---

## 5. Guía de Pruebas y Validación

### Verificación de logs
1. Conectar el dispositivo y abrir Logcat.
2. Filtrar por `FCM_DIAG`.
3. Copiar la cadena alfanumérica larga resultante.

### Prueba mediante Firebase Console
1. Ir a **Cloud Messaging** en la consola de Firebase.
2. Crear un mensaje tipo "Notificación".
3. Enviar un mensaje de prueba utilizando el token copiado.
4. **Validación esperada**:
   - Si la app está cerrada: Aparece la notificación del sistema.
   - Si la app está abierta: Se ejecuta `mostrarNotificacion` y aparece el banner superior.

## 6. Resolución de Problemas (Troubleshooting)
*   **Error de Almacenamiento KSP**: Si la compilación falla en `:app:kspDebugKotlin`, realizar un `Clean Project` y eliminar la carpeta `.gradle` de la raíz.
*   **Servicios de Google Play**: En emuladores, verificar que la imagen del sistema tenga "Google Play Store".
*   **google-services.json**: Asegurarse de que el `applicationId` (`com.kmsoft.lacafeteria`) coincida exactamente con el configurado en la consola de Firebase.
