package com.kironillo.data.hardware

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.kironillo.domain.model.PermissionStatus
import com.kironillo.domain.model.PermissionType
import com.kironillo.domain.repository.HardwarePermissionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class HardwarePermissionRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val dataStore: DataStore<Preferences>,
) : HardwarePermissionRepository {

    override fun getPermissionStatus(type: PermissionType): PermissionStatus {
        val granted = ContextCompat.checkSelfPermission(context, type.manifestName) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) return PermissionStatus.GRANTED

        val activity = context as? Activity ?: return PermissionStatus.NOT_REQUESTED
        val shouldShow = ActivityCompat.shouldShowRequestPermissionRationale(activity, type.manifestName)

        return when {
            shouldShow -> PermissionStatus.DENIED       // отказали, но можно переспросить
            wasRequestedBefore(type) -> PermissionStatus.PERMANENTLY_DENIED  // «не спрашивать»
            else -> PermissionStatus.NOT_REQUESTED      // ещё не спрашивали
        }
    }

    override fun wasRequestedBefore(type: PermissionType): Boolean {
        return runBlocking {
            dataStore.data.first()[requestedKey(type)] ?: false
        }
    }

    override suspend fun markAsRequested(type: PermissionType) {
        dataStore.edit { it[requestedKey(type)] = true }
    }

    private fun requestedKey(type: PermissionType) = booleanPreferencesKey("perm_requested_${type.name}")

    override fun getRationale(type: PermissionType): String {
        return when (type) {
            PermissionType.CAMERA -> "Для того, чтобы делать фотографии заданий, приложению нужен доступ к камере."
            PermissionType.LOCATION -> "Для сохранения фотографий на карте и определения вашего местоположения нужен доступ к геолокации."
        }
    }

    override fun isPermanentlyDenied(type: PermissionType): Boolean {
        return getPermissionStatus(type) == PermissionStatus.PERMANENTLY_DENIED
    }


}
