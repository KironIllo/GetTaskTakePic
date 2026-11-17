package com.kironillo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kironillo.domain.result.DomainResult
import com.kironillo.domain.usecase.auth.LogoutUseCase
import com.kironillo.domain.usecase.task.GetTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val getTasksUseCase: GetTasksUseCase,
        private val logoutUseCase: LogoutUseCase,
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow<HomeUiState>(HomeUiState.Loading)

        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

        private val _logoutError = MutableStateFlow<String?>(null)
        val logoutError: StateFlow<String?> = _logoutError.asStateFlow()

        private val _isLoggingOut = MutableStateFlow(false)
        val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()

        init {
            loadTasks()
        }

        fun loadTasks() {
            viewModelScope.launch {
                _uiState.value = HomeUiState.Loading

                when (val result = getTasksUseCase()) {
                    is DomainResult.Success -> {
                        _uiState.value =
                            if (result.data.isEmpty()) {
                                HomeUiState.Empty
                            } else {
                                HomeUiState.Content(result.data)
                            }
                    }

                    is DomainResult.Error -> {
                        _uiState.value =
                            HomeUiState.Error(
                                message = "Не удалось загрузить задачи. Попробуйте ещё раз.",
                            )
                    }
                }
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
                } catch (exception: Exception) {
                    _logoutError.value =
                        exception.message ?: "Не удалось выйти из аккаунта"
                } finally {
                    _isLoggingOut.value = false
                }
            }
        }
    }
