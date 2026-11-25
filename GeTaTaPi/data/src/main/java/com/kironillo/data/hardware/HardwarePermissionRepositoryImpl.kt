package com.kironillo.data.hardware

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kironillo.domain.model.PermissionStatus
import com.kironillo.domain.model.PermissionType
import com.kironillo.domain.repository.HardwarePermissionRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class HardwarePermissionRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : HardwarePermissionRepository {

    override fun getPermissionStatus(type: PermissionType): PermissionStatus {
        val isGranted = ContextCompat.checkSelfPermission(context, type.manifestName) == PackageManager.PERMISSION_GRANTED
        if (isGranted) return PermissionStatus.GRANTED

        val activity = context as? Activity ?: return PermissionStatus.NOT_REQUESTED
        val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, type.manifestName)

        return if (shouldShowRationale) {
            PermissionStatus.DENIED
        } else {
            // Если разрешение не дано и rationale не нужен, значит либо еще не запрашивали, либо отклонили навсегда
            PermissionStatus.PERMANENTLY_DENIED // Для простоты считаем так
        }
    }

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
