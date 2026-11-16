package com.kironillo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kironillo.domain.result.DomainResult
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
    ) : ViewModel() {
        private val _uiState =
            MutableStateFlow<HomeUiState>(
                HomeUiState.Loading,
            )

        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

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
    }
