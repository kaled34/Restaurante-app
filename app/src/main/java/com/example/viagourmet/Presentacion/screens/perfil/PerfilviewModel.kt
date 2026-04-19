package com.example.viagourmet.Presentacion.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viagourmet.Presentacion.session.SessionManager
import com.example.viagourmet.Presentacion.session.UsuarioSesion
import com.example.viagourmet.util.LocationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PerfilViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val locationService: LocationService
) : ViewModel() {

    private val _usuario = MutableStateFlow<UsuarioSesion?>(null)
    val usuario: StateFlow<UsuarioSesion?> = _usuario

    private val _ubicacion = MutableStateFlow<String>("Obteniendo ubicación...")
    val ubicacion: StateFlow<String> = _ubicacion

    private val _isFakeGpsDetected = MutableStateFlow(false)
    val isFakeGpsDetected: StateFlow<Boolean> = _isFakeGpsDetected

    init {
        _usuario.value = sessionManager.obtenerSesion()
        obtenerUbicacionActual()
    }

    fun obtenerUbicacionActual() {
        viewModelScope.launch {
            val location = locationService.getCurrentLocation()
            
            // VERIFICACIÓN DE FAKE GPS
            val isMocked = locationService.isLocationMocked(location) || locationService.isMockLocationSettingEnabled()
            _isFakeGpsDetected.value = isMocked

            if (location != null && !isMocked) {
                val direccion = locationService.getAddressFromLocation(location.latitude, location.longitude)
                _ubicacion.value = direccion
            } else if (isMocked) {
                _ubicacion.value = "Ubicación Bloqueada (Simulación detectada)"
            } else {
                _ubicacion.value = "Ubicación no disponible"
            }
        }
    }

    fun cerrarSesion() {
        sessionManager.cerrarSesion()
    }
}
