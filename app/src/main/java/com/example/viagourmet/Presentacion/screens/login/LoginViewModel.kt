package com.example.viagourmet.Presentacion.screens.login

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

data class LoginUiState(
    val isLoading: Boolean = false,
    val loginExitoso: Boolean = false,
    val rolLogueado: RolUsuario? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String, rol: RolUsuario) {
        viewModelScope.launch {
            _uiState.value = LoginUiState(isLoading = true)

            when (val result = authRepository.login(email, password)) {
                is AuthResult.Success -> {
                    // Verificar que el rol coincida
                    if (result.usuario.rol != rol) {
                        _uiState.value = LoginUiState(
                            errorMessage = "Esta cuenta es de tipo '${result.usuario.rol.name.lowercase()}'. " +
                                    "Selecciona el rol correcto."
                        )
                        return@launch
                    }
                    sessionManager.guardarSesion(result.usuario)
                    _uiState.value = LoginUiState(
                        loginExitoso = true,
                        rolLogueado = result.usuario.rol
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = LoginUiState(errorMessage = result.mensaje)
                }
            }
        }
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}