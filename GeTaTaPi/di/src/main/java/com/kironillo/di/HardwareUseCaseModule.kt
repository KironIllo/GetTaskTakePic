package com.kironillo.di

import com.kironillo.domain.hardware.LocationProvider
import com.kironillo.domain.repository.SettingsRepository
import com.kironillo.domain.usecase.photo.GetCurrentLocationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class HardwareUseCaseModule {

    @Provides
    @Singleton
    fun provideGetCurrentLocationUseCase(
        locationProvider: LocationProvider,
        settingsRepository: SettingsRepository,
    ) = GetCurrentLocationUseCase(locationProvider, settingsRepository)
}
