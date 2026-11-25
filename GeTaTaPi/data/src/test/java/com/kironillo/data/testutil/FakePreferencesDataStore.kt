package com.kironillo.data.testutil

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakePreferencesDataStore(
    initialPreferences: Preferences = emptyPreferences(),
) : DataStore<Preferences> {

    private val state = MutableStateFlow(initialPreferences)

    override val data: Flow<Preferences> = state

    override suspend fun updateData(
        transform: suspend (t: Preferences) -> Preferences,
    ): Preferences {
        // Безопасное обновление через update (атомарно для StateFlow)
        var newValue: Preferences? = null
        state.update { current ->
            // transform может быть suspend, но внутри update мы не можем его вызвать.
            // Поэтому используем простой подход: вызовем transform вне update и затем присвоим.
            // Для потокобезопасности в тестах (один поток) этого достаточно.
            val transformed = kotlinx.coroutines.runBlocking { transform(current) }
            newValue = transformed
            transformed
        }
        return newValue!!
    }
}
