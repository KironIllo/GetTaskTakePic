package com.kironillo.data.repository

import com.kironillo.data.local.datastore.SettingsDataStore
import com.kironillo.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
) : SettingsRepository {

    override suspend fun setGeolocationEnabled(enabled: Boolean) {
        settingsDataStore.setGeolocationEnabled(enabled)
    }

    override suspend fun isGeolocationEnabled(): Boolean {
        return settingsDataStore.isGeolocationEnabled.first()
    }
}
