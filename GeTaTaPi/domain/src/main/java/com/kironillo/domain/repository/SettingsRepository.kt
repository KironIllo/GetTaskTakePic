package com.kironillo.domain.repository

interface SettingsRepository {
    suspend fun setGeolocationEnabled(enabled: Boolean)

    suspend fun isGeolocationEnabled(): Boolean
}
