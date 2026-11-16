package com.kironillo.di

import com.kironillo.domain.repository.TaskRepository
import com.kironillo.domain.usecase.task.GetTasksUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideGetTasksUseCase(
        taskRepository: TaskRepository,
    ): GetTasksUseCase {
        return GetTasksUseCase(taskRepository)
    }
}
