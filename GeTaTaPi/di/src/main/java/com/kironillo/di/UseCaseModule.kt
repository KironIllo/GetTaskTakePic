package com.kironillo.di

import com.kironillo.domain.repository.TaskRepository
import com.kironillo.domain.usecase.task.CreateTaskUseCase
import com.kironillo.domain.usecase.task.GetTasksUseCase
import com.kironillo.domain.usecase.task.GetTopicsUseCase
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
    @Provides
    fun provideGetTopicsUseCase(taskRepository: TaskRepository): GetTopicsUseCase =
        GetTopicsUseCase(taskRepository)

    @Provides
    fun provideCreateTaskUseCase(taskRepository: TaskRepository): CreateTaskUseCase =
        CreateTaskUseCase(taskRepository)
}
