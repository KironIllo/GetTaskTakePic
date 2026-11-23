package com.kironillo.presentation.viewmodel

import com.kironillo.domain.model.SubmissionResult

sealed interface ReviewUiState {
    data object Idle : ReviewUiState

    data object Loading : ReviewUiState

    data class Processing(
        val submissionId: String,
    ) : ReviewUiState

    data class Success(
        val result: SubmissionResult,
    ) : ReviewUiState

    data class Error(
        val message: String,
    ) : ReviewUiState
}
