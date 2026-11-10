package com.kironillo.domain.usecase.task

import com.kironillo.domain.model.Task
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.domain.repository.TaskRepository
import com.kironillo.domain.result.DomainError
import com.kironillo.domain.result.DomainResult

class CreateTaskUseCase(
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(
        topic: String,
        difficulty: TaskDifficulty,
    ): DomainResult<Task> {
        return try {
            val task = taskRepository.createTask(topic, difficulty)
            DomainResult.Success(task)
        } catch (_: Exception) {
            DomainResult.Error(DomainError.Unknown)
        }
    }
}
