package com.kironillo.data.local.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.io.path.createTempDirectory

class SettingsDataStoreTest {

    @Test
    fun `geolocation is disabled by default`() = runBlocking {
        val dataStore = createSettingsDataStore()

        assertFalse(dataStore.isGeolocationEnabled.first())
    }

    @Test
    fun `saves and reads geolocation setting`() = runBlocking {
        val dataStore = createSettingsDataStore()

        dataStore.setGeolocationEnabled(true)

        assertTrue(dataStore.isGeolocationEnabled.first())

        dataStore.setGeolocationEnabled(false)

        assertFalse(dataStore.isGeolocationEnabled.first())
    }


    @Test
    fun `theme mode defaults to system and persists selection`() = runBlocking {
        val dataStore = createSettingsDataStore()

        assertTrue(dataStore.themeMode.first() == "SYSTEM")
        dataStore.setThemeMode("DARK")
        assertTrue(dataStore.themeMode.first() == "DARK")
    }

    private fun createSettingsDataStore(): SettingsDataStore {
        val directory = createTempDirectory("settings-test").toFile()
        val file = File(directory, "settings.preferences_pb")

        val preferencesDataStore = PreferenceDataStoreFactory.create(
            produceFile = { file },
        )

        return SettingsDataStore(preferencesDataStore)
    }
}
