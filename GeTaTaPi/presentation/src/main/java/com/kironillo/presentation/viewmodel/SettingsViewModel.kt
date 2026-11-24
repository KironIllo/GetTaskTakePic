package com.kironillo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kironillo.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLoading: Boolean = true,
    val geolocationEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val themeMode: String = "SYSTEM",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.observeThemeMode(),
                kotlinx.coroutines.flow.flow {
                    emit(settingsRepository.isGeolocationEnabled() to settingsRepository.areNotificationsEnabled())
                },
            ) { theme, toggles ->
                SettingsUiState(false, toggles.first, toggles.second, theme)
            }.collect { _uiState.value = it }
        }
    }

    fun setGeolocationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setGeolocationEnabled(enabled)
            _uiState.value = _uiState.value.copy(geolocationEnabled = enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
            _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
            _uiState.value = _uiState.value.copy(themeMode = mode)
        }
    }
}
