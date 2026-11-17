package com.kironillo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kironillo.domain.model.AuthRegistrationResult
import com.kironillo.domain.repository.AuthRepository
import com.kironillo.domain.usecase.auth.LoginUseCase
import com.kironillo.domain.usecase.auth.LogoutUseCase
import com.kironillo.domain.usecase.auth.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class AuthViewModel
    @Inject
    constructor(
        private val loginUseCase: LoginUseCase,
        private val registerUseCase: RegisterUseCase,
        private val logoutUseCase: LogoutUseCase,
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow<AuthUiState>(AuthUiState.CheckingSession)

        val uiState = _uiState.asStateFlow()

        init {
            restoreSession()
        }

        private fun restoreSession() {
            viewModelScope.launch {
                try {
                    val userId = authRepository.restoreSession()

                    _uiState.value =
                        if (userId != null) {
                            AuthUiState.Success(userId)
                        } else {
                            AuthUiState.Idle
                        }
                } catch (exception: Exception) {
                    _uiState.value =
                        AuthUiState.Error(
                            getAuthErrorMessage(exception) ?: "Не удалось восстановить сессию",
                        )
                }
            }
        }

        fun login(
            email: String,
            password: String,
        ) {
            authenticate {
                AuthUiState.Success(
                    loginUseCase(email.trim(), password),
                )
            }
        }

        fun register(
            email: String,
            password: String,
        ) {
            viewModelScope.launch {
                _uiState.value = AuthUiState.Loading

                try {
                    _uiState.value =
                        when (
                            val result =
                                registerUseCase(
                                    email.trim(),
                                    password,
                                )
                        ) {
                            is AuthRegistrationResult.Authenticated ->
                                AuthUiState.Success(result.userId)

                            AuthRegistrationResult.EmailConfirmationRequired ->
                                AuthUiState.EmailConfirmationRequired
                        }
                } catch (exception: Exception) {
                    _uiState.value =
                        AuthUiState.Error(
                            getAuthErrorMessage(exception) ?: "Ошибка регистрации",
                        )
                }
            }
        }

        fun logout() {
            viewModelScope.launch {
                try {
                    logoutUseCase()
                    _uiState.value = AuthUiState.Idle
                } catch (exception: Exception) {
                    _uiState.value =
                        AuthUiState.Error(
                            getAuthErrorMessage(exception) ?: "Не удалось выйти из аккаунта",
                        )
                }
            }
        }

        private fun authenticate(action: suspend () -> AuthUiState) {
            viewModelScope.launch {
                _uiState.value = AuthUiState.Loading

                try {
                    _uiState.value = action()
                } catch (exception: Exception) {
                    _uiState.value =
                        AuthUiState.Error(
                            getAuthErrorMessage(exception) ?: "Ошибка авторизации",
                        )
                }
            }
        }

        fun resetState() {
            _uiState.value = AuthUiState.Idle
        }

        private fun getAuthErrorMessage(exception: Exception): String? =
            when (exception) {
                is HttpException -> {
                    when (exception.code()) {
                        429 -> "Слишком много попыток. Попробуйте позже."
                        400 -> "Проверьте email и пароль."
                        401 -> "Неверный email или пароль."
                        else -> "Ошибка сервера. Попробуйте позже."
                    }
                }

                else -> exception.message
            }
    }
