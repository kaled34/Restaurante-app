package com.example.viagourmet.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.provider.Settings
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).await()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Detecta si la ubicación proporcionada es simulada (Fake GPS).
     */
    fun isLocationMocked(location: Location?): Boolean {
        if (location == null) return false
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            // Versiones anteriores
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }
    }

    /**
     * Verifica si el modo de ubicaciones de prueba está habilitado en el sistema.
     * (Medida adicional de seguridad)
     */
    fun isMockLocationSettingEnabled(): Boolean {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ALLOW_MOCK_LOCATION) != "0"
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String {
        return withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val feature = address.featureName ?: ""
                    val street = address.thoroughfare ?: ""
                    val city = address.locality ?: address.subAdminArea ?: ""
                    val state = address.adminArea ?: ""
                    
                    listOf(feature, street, city, state)
                        .filter { it.isNotBlank() }
                        .joinToString(", ")
                } else {
                    "Dirección no encontrada"
                }
            } catch (e: Exception) {
                "Error al obtener dirección"
            }
        }
    }
}
