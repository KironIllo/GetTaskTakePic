package com.kironillo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kironillo.domain.usecase.photo.GetCurrentLocationUseCase
import com.kironillo.domain.usecase.photo.GetSubmissionStatusUseCase
import com.kironillo.domain.usecase.photo.SubmitPhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel
    @Inject
    constructor(
        private val submitPhotoUseCase: SubmitPhotoUseCase,
        private val getSubmissionStatusUseCase: GetSubmissionStatusUseCase,
        private val getCurrentLocationUseCase: GetCurrentLocationUseCase,
    ) : ViewModel() {
        private var pendingIdempotencyKey: String? = null
        private var pendingRequestIdentity: String? = null
        private val _uiState =
            MutableStateFlow<ReviewUiState>(ReviewUiState.Idle)
        val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

        private var pollingJob: Job? = null

        fun submitPhoto(
            taskId: String,
            photoPath: String,
        ) {
            if (_uiState.value !is ReviewUiState.Idle &&
                _uiState.value !is ReviewUiState.Error
            ) {
                return
            }

            val requestIdentity = "$taskId|$photoPath"

            if (pendingRequestIdentity != requestIdentity) {
                pendingRequestIdentity = requestIdentity
                pendingIdempotencyKey = UUID.randomUUID().toString()
            }

            val idempotencyKey =
                pendingIdempotencyKey
                    ?: UUID.randomUUID().toString().also {
                        pendingIdempotencyKey = it
                    }

            viewModelScope.launch {
                _uiState.value = ReviewUiState.Loading
                val geoPoint = runCatching { getCurrentLocationUseCase() }.getOrNull()
                try {
                    val submissionId =
                        submitPhotoUseCase(
                            taskId = taskId,
                            photoPath = photoPath,
                            idempotencyKey = idempotencyKey,
                            latitude = geoPoint?.latitude,
                            longitude = geoPoint?.longitude,
                        )
                    pollSubmissionStatus(submissionId)
                } catch (exception: Exception) {
                    _uiState.value =
                        ReviewUiState.Error(
                            exception.message ?: "Не удалось отправить фотографию",
                        )
                }
            }
        }

        private fun pollSubmissionStatus(submissionId: String) {
            pollingJob?.cancel()

            pollingJob =
                viewModelScope.launch {
                    try {
                        while (true) {
                            val result = getSubmissionStatusUseCase(submissionId)

                            when (result.status.lowercase()) {
                                "completed" -> {
                                    _uiState.value = ReviewUiState.Success(result)
                                    break
                                }

                                "failed" -> {
                                    pendingIdempotencyKey = null

                                    _uiState.value =
                                        ReviewUiState.Error(
                                            result.feedback ?: "Не удалось проверить фотографию",
                                        )
                                    break
                                }

                                else -> {
                                    _uiState.value =
                                        ReviewUiState.Processing(submissionId)
                                    delay(POLLING_INTERVAL)
                                }
                            }
                        }
                    } catch (exception: Exception) {
                        _uiState.value =
                            ReviewUiState.Error(
                                exception.message ?: "Не удалось получить результат проверки",
                            )
                    }
                }
        }

        private companion object {
            const val POLLING_INTERVAL = 3_000L
        }
    }
