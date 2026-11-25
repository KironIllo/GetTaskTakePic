package com.kironillo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kironillo.domain.hardware.LocationProvider
import com.kironillo.domain.model.PermissionStatus
import com.kironillo.domain.model.PermissionType
import com.kironillo.domain.repository.HardwarePermissionRepository
import com.kironillo.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isLoading: Boolean = true,
    val geolocationEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val themeMode: String = "SYSTEM",
)

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
        private val permissionRepository: HardwarePermissionRepository,
        private val locationProvider: LocationProvider,
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

        fun canUseGeolocation(): Boolean {
            val permission = permissionRepository.getPermissionStatus(PermissionType.LOCATION)
            return permission == PermissionStatus.GRANTED && locationProvider.isSystemLocationEnabled()
        }

        fun geolocationUnavailableReason(): String? {
            val permission = permissionRepository.getPermissionStatus(PermissionType.LOCATION)
            return when {
                permission != PermissionStatus.GRANTED -> "Нужно разрешение на местоположение"
                !locationProvider.isSystemLocationEnabled() -> "Включите GPS в настройках телефона"
                else -> null
            }
        }
    }
