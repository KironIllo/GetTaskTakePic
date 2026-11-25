package com.kironillo.data.repository

import com.kironillo.data.local.datastore.SettingsDataStore
import com.kironillo.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) : SettingsRepository {
    override suspend fun setGeolocationEnabled(enabled: Boolean) = settingsDataStore.setGeolocationEnabled(enabled)
    override suspend fun isGeolocationEnabled(): Boolean = settingsDataStore.getGeolocationEnabled()
    override suspend fun setNotificationsEnabled(enabled: Boolean) = settingsDataStore.setNotificationsEnabled(enabled)
    override suspend fun areNotificationsEnabled(): Boolean = settingsDataStore.getNotificationsEnabled()
    override fun observeThemeMode(): Flow<String> = settingsDataStore.themeMode
    override suspend fun setThemeMode(mode: String) = settingsDataStore.setThemeMode(mode)
}
