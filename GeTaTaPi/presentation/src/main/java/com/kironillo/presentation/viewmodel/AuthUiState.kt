package com.kironillo.presentation.viewmodel

sealed interface AuthUiState {
    data object CheckingSession : AuthUiState

    data object Idle : AuthUiState

    data object Loading : AuthUiState

    data class Success(
        val userId: String,
    ) : AuthUiState

    data object EmailConfirmationRequired : AuthUiState

    data class Error(
        val message: String,
    ) : AuthUiState
}
