package com.example.viagourmet.Presentacion.screens.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viagourmet.Presentacion.session.RolUsuario
import com.example.viagourmet.Presentacion.session.SessionManager
import com.example.viagourmet.data.repository.AuthRepository
import com.example.viagourmet.data.repository.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegistroUiState(
    val isLoading: Boolean = false,
    val registroExitoso: Boolean = false,
    val rolRegistrado: RolUsuario? = null,
    val errorMessage: String? = null
)

sealed class RegistroEvent {
    data class Registrar(
        val nombre: String, val apellido: String?, val telefono: String?,
        val email: String, val password: String, val confirmarPassword: String,
        val rol: RolUsuario, val fotoCredencialUri: String? = null
    ) : RegistroEvent()
    object LimpiarError : RegistroEvent()
}

@HiltViewModel
class RegistroViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    fun onEvent(event: RegistroEvent) {
        when (event) {
            is RegistroEvent.Registrar -> registrar(event)
            is RegistroEvent.LimpiarError -> _uiState.value = _uiState.value.copy(errorMessage = null)
        }
    }

    private fun registrar(event: RegistroEvent.Registrar) {
        viewModelScope.launch {
            _uiState.value = RegistroUiState(isLoading = true)
            when (val result = authRepository.registrar(
                nombre = event.nombre, apellido = event.apellido, telefono = event.telefono,
                email = event.email, password = event.password, confirmarPassword = event.confirmarPassword,
                rol = event.rol, fotoCredencialUri = event.fotoCredencialUri
            )) {
                is AuthResult.Success -> {
                    sessionManager.guardarSesion(result.usuario)
                    _uiState.value = RegistroUiState(registroExitoso = true, rolRegistrado = result.usuario.rol)
                }
                is AuthResult.Error -> {
                    _uiState.value = RegistroUiState(errorMessage = result.mensaje)
                }
            }
        }
    }
}
