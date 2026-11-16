package com.kironillo.domain.usecase.task

import com.kironillo.domain.model.Task
import com.kironillo.domain.repository.TaskRepository
import com.kironillo.domain.result.DomainError
import com.kironillo.domain.result.DomainException
import com.kironillo.domain.result.DomainResult
import kotlin.coroutines.cancellation.CancellationException

class GetTasksUseCase(
    private val taskRepository: TaskRepository,
) {
    suspend operator fun invoke(): DomainResult<List<Task>> {
        return try {
            DomainResult.Success(taskRepository.getTasks())
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: DomainException) {
            DomainResult.Error(exception.domainError)
        } catch (_: Exception) {
            DomainResult.Error(DomainError.Unknown)
        }
    }
}
