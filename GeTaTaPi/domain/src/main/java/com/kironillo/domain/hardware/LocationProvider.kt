package com.kironillo.domain.hardware

data class GeoPoint(val latitude: Double, val longitude: Double)

interface LocationProvider {
    suspend fun getCurrentLocation(): GeoPoint?

    fun isSystemLocationEnabled(): Boolean
}
