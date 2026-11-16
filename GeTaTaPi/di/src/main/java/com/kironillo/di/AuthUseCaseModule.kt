package com.kironillo.di

import com.kironillo.domain.repository.AuthRepository
import com.kironillo.domain.usecase.auth.LoginUseCase
import com.kironillo.domain.usecase.auth.LogoutUseCase
import com.kironillo.domain.usecase.auth.RegisterUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AuthUseCaseModule {

    @Provides
    fun provideLoginUseCase(
        repository: AuthRepository,
    ) = LoginUseCase(repository)

    @Provides
    fun provideRegisterUseCase(
        repository: AuthRepository,
    ) = RegisterUseCase(repository)

    @Provides
    fun provideLogoutUseCase(
        repository: AuthRepository,
    ) = LogoutUseCase(repository)
}
