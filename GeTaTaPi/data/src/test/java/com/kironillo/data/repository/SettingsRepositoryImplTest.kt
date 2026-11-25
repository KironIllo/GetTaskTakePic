package com.kironillo.data.repository

import com.kironillo.data.local.datastore.SettingsDataStore
import com.kironillo.data.testutil.FakePreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsRepositoryImplTest {

    @Test
    fun `geolocation is disabled by default`() = runBlocking {
        assertFalse(createRepository().isGeolocationEnabled())
    }

    @Test
    fun `saves enabled geolocation setting`() = runBlocking {
        val repository = createRepository()
        repository.setGeolocationEnabled(true)
        assertTrue(repository.isGeolocationEnabled())
    }

    @Test
    fun `saves disabled geolocation setting`() = runBlocking {
        val repository = createRepository()
        repository.setGeolocationEnabled(true)
        repository.setGeolocationEnabled(false)
        assertFalse(repository.isGeolocationEnabled())
    }

    @Test
    fun `notifications are enabled by default`() = runBlocking {
        assertTrue(createRepository().areNotificationsEnabled())
    }

    @Test
    fun `saves notifications preference`() = runBlocking {
        val repository = createRepository()
        repository.setNotificationsEnabled(false)
        assertFalse(repository.areNotificationsEnabled())
    }

    @Test
    fun `theme mode is persisted`() = runBlocking {
        val repository = createRepository()
        repository.setThemeMode("DARK")
        assertTrue(repository.observeThemeMode().first() == "DARK")
    }

    private fun createRepository(): SettingsRepositoryImpl {
        val dataStore = FakePreferencesDataStore()
        return SettingsRepositoryImpl(SettingsDataStore(dataStore))
    }
}
