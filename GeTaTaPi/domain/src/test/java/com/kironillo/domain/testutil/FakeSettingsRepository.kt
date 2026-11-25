package com.kironillo.domain.testutil

import com.kironillo.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Фейковый репозиторий настроек: хранит значения в памяти.
 * Позволяет задать начальное состояние и проверить побочные эффекты.
 */
class FakeSettingsRepository(
    geolocationEnabled: Boolean = false,
    notificationsEnabled: Boolean = true,
    themeMode: String = "SYSTEM",
) : SettingsRepository {

    private val geolocation = MutableStateFlow(geolocationEnabled)
    private val notifications = MutableStateFlow(notificationsEnabled)
    private val theme = MutableStateFlow(themeMode)

    override suspend fun isGeolocationEnabled(): Boolean = geolocation.value

    override suspend fun setGeolocationEnabled(enabled: Boolean) {
        geolocation.value = enabled
    }

    override suspend fun areNotificationsEnabled(): Boolean = notifications.value

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        notifications.value = enabled
    }

    override fun observeThemeMode(): Flow<String> = theme.asStateFlow()

    override suspend fun setThemeMode(mode: String) {
        theme.value = mode
    }
}
