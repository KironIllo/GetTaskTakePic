package com.kironillo.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.kironillo.data.local.datastore.SettingsDataStore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class SettingsRepositoryImplTest {

    @Test
    fun `geolocation is disabled by default`() = runBlocking {
        val repository = createRepository()

        assertFalse(repository.isGeolocationEnabled())
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
        val repository = createRepository()

        assertTrue(repository.areNotificationsEnabled())
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
        val directory = kotlin.io.path.createTempDirectory().toFile()
        val file = File(directory, "settings.preferences_pb")

        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { file },
        )

        return SettingsRepositoryImpl(
            SettingsDataStore(dataStore),
        )
    }
}
