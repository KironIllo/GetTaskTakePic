package com.kironillo.data.hardware

import com.kironillo.domain.model.PermissionStatus
import com.kironillo.domain.model.PermissionType
import com.kironillo.domain.repository.HardwarePermissionRepository

class FakeHardwarePermissionRepository : HardwarePermissionRepository {
    private val statuses = mutableMapOf<PermissionType, PermissionStatus>()

    fun setStatus(type: PermissionType, status: PermissionStatus) {
        statuses[type] = status
    }

    override fun getPermissionStatus(type: PermissionType): PermissionStatus {
        return statuses[type] ?: PermissionStatus.NOT_REQUESTED
    }

    override fun getRationale(type: PermissionType): String = "Fake rationale for $type"

    override fun isPermanentlyDenied(type: PermissionType): Boolean {
        return getPermissionStatus(type) == PermissionStatus.PERMANENTLY_DENIED
    }
}
