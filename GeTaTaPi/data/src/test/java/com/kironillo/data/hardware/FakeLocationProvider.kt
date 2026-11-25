package com.kironillo.data.hardware

import com.kironillo.domain.hardware.GeoPoint
import com.kironillo.domain.hardware.LocationProvider

class FakeLocationProvider(
    var nextLocation: GeoPoint? = null,
    var shouldThrow: Boolean = false,
    var isEnabled: Boolean = false
) : LocationProvider {
    override suspend fun getCurrentLocation(): GeoPoint? {
        if (shouldThrow) throw RuntimeException("Location error")
        return nextLocation
    }

    override fun isSystemLocationEnabled() = isEnabled
}
