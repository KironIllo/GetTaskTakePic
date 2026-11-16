package com.kironillo.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(
    name = "app_settings",
)

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private companion object {
        val GEOLOCATION_ENABLED = booleanPreferencesKey("geolocation_enabled")
    }

    val isGeolocationEnabled: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[GEOLOCATION_ENABLED] ?: false
        }

    suspend fun setGeolocationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[GEOLOCATION_ENABLED] = enabled
        }
    }
}
