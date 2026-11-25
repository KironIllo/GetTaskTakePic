package com.kironillo.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kironillo.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
    ) : ViewModel() {
        val themeMode: StateFlow<String> =
            settingsRepository
                .observeThemeMode()
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "SYSTEM")

        fun setThemeMode(mode: String) {
            if (mode !in setOf("SYSTEM", "LIGHT", "DARK")) return
            viewModelScope.launch { settingsRepository.setThemeMode(mode) }
        }
    }
