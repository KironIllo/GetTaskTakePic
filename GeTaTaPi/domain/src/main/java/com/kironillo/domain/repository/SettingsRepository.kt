package com.kironillo.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    suspend fun setGeolocationEnabled(enabled: Boolean)
    suspend fun isGeolocationEnabled(): Boolean
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun areNotificationsEnabled(): Boolean
    fun observeThemeMode(): Flow<String>
    suspend fun setThemeMode(mode: String)
}
