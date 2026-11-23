package com.kironillo.presentation.viewmodel

import com.kironillo.domain.model.SubmissionHistoryItem

sealed interface HistoryUiState {
    data object Loading : HistoryUiState

    data class Success(
        val items: List<SubmissionHistoryItem>,
        val hasMore: Boolean,
        val isLoadingMore: Boolean = false,
        val loadMoreError: String? = null,
    ) : HistoryUiState

    data class Error(
        val message: String,
    ) : HistoryUiState
}
