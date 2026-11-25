package com.kironillo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kironillo.domain.model.Task
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.domain.model.Topic
import com.kironillo.domain.result.DomainResult
import com.kironillo.domain.usecase.auth.LogoutUseCase
import com.kironillo.domain.usecase.task.CreateTaskUseCase
import com.kironillo.domain.usecase.task.GetTasksUseCase
import com.kironillo.domain.usecase.task.GetTopicsUseCase
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
        private val createTaskUseCase: CreateTaskUseCase,
        private val getTopicsUseCase: GetTopicsUseCase,
        private val logoutUseCase: LogoutUseCase,
    ) : ViewModel() {
        private var lastLoadAttemptAt = 0L
        private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
        val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
        private val _topics = MutableStateFlow<List<Topic>>(emptyList())
        val topics: StateFlow<List<Topic>> = _topics.asStateFlow()
        private val _selectedTopicId = MutableStateFlow<String?>(null)
        val selectedTopicId: StateFlow<String?> = _selectedTopicId.asStateFlow()
        private val _selectedDifficulty = MutableStateFlow(TaskDifficulty.EASY)
        val selectedDifficulty: StateFlow<TaskDifficulty> = _selectedDifficulty.asStateFlow()
        private val _isCreatingTask = MutableStateFlow(false)
        val isCreatingTask: StateFlow<Boolean> = _isCreatingTask.asStateFlow()
        private val _createTaskError = MutableStateFlow<String?>(null)
        val createTaskError: StateFlow<String?> = _createTaskError.asStateFlow()
        private val _logoutError = MutableStateFlow<String?>(null)
        val logoutError: StateFlow<String?> = _logoutError.asStateFlow()
        private val _isLoggingOut = MutableStateFlow(false)
        val isLoggingOut: StateFlow<Boolean> = _isLoggingOut.asStateFlow()

        init {
            loadTopics()
            loadTasks()
        }

        private fun loadTopics() =
            viewModelScope.launch {
                when (val result = getTopicsUseCase()) {
                    is DomainResult.Success -> {
                        _topics.value = result.data
                        if (_selectedTopicId.value == null) _selectedTopicId.value = result.data.firstOrNull()?.id
                    }
                    is DomainResult.Error -> _createTaskError.value = "Не удалось загрузить темы. Проверьте подключение к интернету."
                }
            }

        fun selectTopic(id: String) {
            _selectedTopicId.value = id
            _createTaskError.value = null
        }

        fun selectDifficulty(value: TaskDifficulty) {
            _selectedDifficulty.value = value
        }

        fun createTask(onCreated: (Task) -> Unit) {
            val topicId =
                _selectedTopicId.value ?: run {
                    _createTaskError.value = "Выберите тему"
                    return
                }
            if (_isCreatingTask.value) return
            viewModelScope.launch {
                _isCreatingTask.value = true
                _createTaskError.value = null
                when (val result = createTaskUseCase(topicId, _selectedDifficulty.value)) {
                    is DomainResult.Success -> {
                        _isCreatingTask.value = false
                        onCreated(result.data)
                        loadTasks()
                    }
                    is DomainResult.Error -> {
                        _createTaskError.value = "Не удалось создать задание. Попробуйте ещё раз."
                        _isCreatingTask.value = false
                    }
                }
            }
        }

        fun loadTasks() = loadTasks(showLoading = true)

        fun refreshTasks() {
            val now = System.currentTimeMillis()
            if (now - lastLoadAttemptAt < REFRESH_INTERVAL_MS) return
            loadTasks(showLoading = _uiState.value !is HomeUiState.Content && _uiState.value != HomeUiState.Empty)
        }

        private fun loadTasks(showLoading: Boolean) {
            lastLoadAttemptAt = System.currentTimeMillis()
            viewModelScope.launch {
                if (showLoading) _uiState.value = HomeUiState.Loading
                when (val result = getTasksUseCase()) {
                    is DomainResult.Success ->
                        _uiState.value =
                            if (result.data.isEmpty()) HomeUiState.Empty else HomeUiState.Content(result.data)
                    is DomainResult.Error ->
                        if (showLoading ||
                            (_uiState.value !is HomeUiState.Content && _uiState.value != HomeUiState.Empty)
                        ) {
                            _uiState.value = HomeUiState.Error("Не удалось загрузить задания. Попробуйте ещё раз.")
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
                } catch (
                    e: Exception,
                ) {
                    _logoutError.value = e.message ?: "Не удалось выйти из аккаунта"
                } finally {
                    _isLoggingOut.value = false
                }
            }
        }

        private companion object {
            const val REFRESH_INTERVAL_MS = 30_000L
        }
    }
