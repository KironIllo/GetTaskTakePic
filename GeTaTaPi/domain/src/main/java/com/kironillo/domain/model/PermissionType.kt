package com.kironillo.domain.model

enum class PermissionType(val manifestName: String) {
    CAMERA("android.permission.CAMERA"),
    LOCATION("android.permission.ACCESS_FINE_LOCATION")
}
