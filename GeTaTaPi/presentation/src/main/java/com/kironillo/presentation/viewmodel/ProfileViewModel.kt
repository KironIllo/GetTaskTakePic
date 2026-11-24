package com.kironillo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kironillo.domain.repository.AuthRepository
import com.kironillo.domain.usecase.auth.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _userId = MutableStateFlow("Загрузка...")
    val userId = _userId.asStateFlow()

    private val _isLoggingOut = MutableStateFlow(false)
    val isLoggingOut = _isLoggingOut.asStateFlow()

    private val _logoutError = MutableStateFlow<String?>(null)
    val logoutError = _logoutError.asStateFlow()

    init {
        viewModelScope.launch {
            _userId.value = runCatching { authRepository.getUserId() }
                .getOrNull() ?: "Не удалось получить ID пользователя"
        }
    }

    fun logout(onSuccess: () -> Unit) {
        if (_isLoggingOut.value) return
        viewModelScope.launch {
            _isLoggingOut.value = true
            _logoutError.value = null
            try {
                logoutUseCase()
                onSuccess()
            } catch (error: Exception) {
                _logoutError.value = error.message ?: "Не удалось выйти из аккаунта"
            } finally {
                _isLoggingOut.value = false
            }
        }
    }
}
