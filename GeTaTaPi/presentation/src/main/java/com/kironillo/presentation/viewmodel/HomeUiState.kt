package com.kironillo.presentation.viewmodel

import com.kironillo.domain.model.Task

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Content(
        val tasks: List<Task>,
    ) : HomeUiState

    data object Empty : HomeUiState

    data class Error(
        val message: String,
    ) : HomeUiState
}
