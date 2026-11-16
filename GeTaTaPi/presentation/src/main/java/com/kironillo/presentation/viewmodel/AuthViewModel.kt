package com.kironillo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kironillo.domain.usecase.auth.LoginUseCase
import com.kironillo.domain.usecase.auth.RegisterUseCase
import com.kironillo.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase,
    private val logoutUseCase: LogoutUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        authenticate {
            loginUseCase(email.trim(), password)
        }
    }

    fun register(email: String, password: String) {
        authenticate {
            registerUseCase(email.trim(), password)
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                logoutUseCase()
                _uiState.value = AuthUiState.Idle
            } catch (exception: Exception) {
                _uiState.value = AuthUiState.Error(
                    exception.message ?: "Не удалось выйти из аккаунта",
                )
            }
        }
    }

    private fun authenticate(action: suspend () -> String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading

            try {
                val userId = action()
                _uiState.value = AuthUiState.Success(userId)
            } catch (exception: Exception) {
                _uiState.value = AuthUiState.Error(
                    exception.message ?: "Ошибка авторизации",
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
