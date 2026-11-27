package com.kironillo.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kironillo.domain.repository.PhotoRepository
import com.kironillo.domain.usecase.GetSubmissionHistoryUseCase
import com.kironillo.presentation.util.toUserMessage
import com.kironillo.presentation.viewmodel.HistoryUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel
    @Inject
    constructor(
        private val getSubmissionHistory: GetSubmissionHistoryUseCase,
        private val photoRepository: PhotoRepository,
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)

        val uiState = _uiState.asStateFlow()

        private var nextCursor: String? = null
        private var isRequestInProgress = false

        companion object {
            private const val PAGE_SIZE = 20
        }

        init {
            loadHistory()
        }

        fun loadHistory() {
            if (isRequestInProgress) return

            viewModelScope.launch {
                isRequestInProgress = true
                _uiState.value = HistoryUiState.Loading
                nextCursor = null

                try {
                    val page =
                        photoRepository.getSubmissionHistory(
                            limit = PAGE_SIZE,
                            cursor = null,
                        )

                    nextCursor = page.nextCursor

                    _uiState.value =
                        HistoryUiState.Success(
                            items = page.items,
                            hasMore = nextCursor != null,
                        )
                } catch (e: Exception) {
                    _uiState.value =
                        HistoryUiState.Error(
                            e.toUserMessage("Не удалось загрузить историю"),
                        )
                } finally {
                    isRequestInProgress = false
                }
            }
        }

        fun loadNextPage() {
            val current =
                _uiState.value as? HistoryUiState.Success
                    ?: return

            if (isRequestInProgress || !current.hasMore) return

            val cursor = nextCursor ?: return

            viewModelScope.launch {
                isRequestInProgress = true

                _uiState.value =
                    current.copy(
                        isLoadingMore = true,
                        loadMoreError = null,
                    )

                try {
                    val page =
                        photoRepository.getSubmissionHistory(
                            limit = PAGE_SIZE,
                            cursor = cursor,
                        )

                    val latest =
                        _uiState.value as? HistoryUiState.Success
                            ?: return@launch

                    val existingIds = latest.items.map { it.id }.toSet()

                    val newItems =
                        page.items.filter {
                            it.id !in existingIds
                        }

                    nextCursor = page.nextCursor

                    _uiState.value =
                        latest.copy(
                            items = latest.items + newItems,
                            hasMore = nextCursor != null,
                            isLoadingMore = false,
                            loadMoreError = null,
                        )
                } catch (e: Exception) {
                    val latest = _uiState.value as? HistoryUiState.Success

                    if (latest != null) {
                        _uiState.value =
                            latest.copy(
                                isLoadingMore = false,
                                loadMoreError =
                                    e.toUserMessage("Не удалось загрузить следующие записи"),
                            )
                    }
                } finally {
                    isRequestInProgress = false
                }
            }
        }

        suspend fun getPhotoUrl(storagePath: String): String = photoRepository.getPhotoUrl(storagePath)
    }
