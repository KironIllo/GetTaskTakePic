package com.kironillo.data.local.datastore

import com.kironillo.data.testutil.FakePreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsDataStoreTest {

    @Test
    fun `geolocation is disabled by default`() = runBlocking {
        val dataStore = FakePreferencesDataStore()
        val settings = SettingsDataStore(dataStore)
        assertFalse(settings.isGeolocationEnabled.first())
    }

    @Test
    fun `saves and reads geolocation setting`() = runBlocking {
        val dataStore = FakePreferencesDataStore()
        val settings = SettingsDataStore(dataStore)

        settings.setGeolocationEnabled(true)
        assertTrue(settings.isGeolocationEnabled.first())

        settings.setGeolocationEnabled(false)
        assertFalse(settings.isGeolocationEnabled.first())
    }

    @Test
    fun `theme mode defaults to system and persists selection`() = runBlocking {
        val dataStore = FakePreferencesDataStore()
        val settings = SettingsDataStore(dataStore)

        assertTrue(settings.themeMode.first() == "SYSTEM")

        settings.setThemeMode("DARK")
        assertTrue(settings.themeMode.first() == "DARK")
    }
}
