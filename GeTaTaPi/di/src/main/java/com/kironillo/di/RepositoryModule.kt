package com.kironillo.di

import com.kironillo.data.repository.TaskRepositoryImpl
import com.kironillo.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindTaskRepository(
        implementation: TaskRepositoryImpl,
    ): TaskRepository
}
