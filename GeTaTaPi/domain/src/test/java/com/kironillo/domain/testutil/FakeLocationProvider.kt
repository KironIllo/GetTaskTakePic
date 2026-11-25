package com.kironillo.domain.testutil

import com.kironillo.domain.hardware.GeoPoint
import com.kironillo.domain.hardware.LocationProvider

/**
 * Фейковый провайдер геолокации для unit-тестов.
 * Позволяет вернуть заранее заданную точку или симулировать ошибку.
 */
class FakeLocationProvider(
    var nextLocation: GeoPoint? = null,
    var shouldThrow: Boolean = false,
    var isEnabled: Boolean = false
) : LocationProvider {
    /** Сколько раз вызывали getCurrentLocation — удобно для проверки кэша. */
    var callCount: Int = 0
        private set

    override suspend fun getCurrentLocation(): GeoPoint? {
        callCount++
        if (shouldThrow) throw RuntimeException("Location unavailable (fake)")
        return nextLocation
    }

    override fun isSystemLocationEnabled() = isEnabled
}
