package com.kironillo.domain.usecase.photo

import com.kironillo.domain.hardware.GeoPoint
import com.kironillo.domain.hardware.LocationProvider
import com.kironillo.domain.repository.SettingsRepository

/**
 * Возвращает координаты для геометки, если пользователь включил геолокацию в настройках.
 * Иначе возвращает null — фото сохранится без метки.
 */
class GetCurrentLocationUseCase(
    private val locationProvider: LocationProvider,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(): GeoPoint? {
        if (!settingsRepository.isGeolocationEnabled()) return null
        return locationProvider.getCurrentLocation()
    }
}
