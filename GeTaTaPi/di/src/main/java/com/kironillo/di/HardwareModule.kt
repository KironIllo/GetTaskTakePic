package com.kironillo.di

import com.kironillo.data.hardware.HardwarePermissionRepositoryImpl
import com.kironillo.data.hardware.LocationProviderImpl
import com.kironillo.domain.hardware.LocationProvider
import com.kironillo.domain.repository.HardwarePermissionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HardwareModule {
    @Binds
    @Singleton
    abstract fun bindHardwarePermissionRepository(
        impl: HardwarePermissionRepositoryImpl
    ): HardwarePermissionRepository

    @Binds
    @Singleton
    abstract fun bindLocationProvider(
        impl: LocationProviderImpl,
    ): LocationProvider
}
