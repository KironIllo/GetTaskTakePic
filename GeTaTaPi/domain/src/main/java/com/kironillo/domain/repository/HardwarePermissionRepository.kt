package com.kironillo.domain.repository

import com.kironillo.domain.model.PermissionStatus
import com.kironillo.domain.model.PermissionType

interface HardwarePermissionRepository {
    fun getPermissionStatus(type: PermissionType): PermissionStatus
    fun getRationale(type: PermissionType): String
    fun isPermanentlyDenied(type: PermissionType): Boolean
}
