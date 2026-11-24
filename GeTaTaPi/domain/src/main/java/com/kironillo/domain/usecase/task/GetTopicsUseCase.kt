package com.kironillo.domain.usecase.task

import com.kironillo.domain.model.Topic
import com.kironillo.domain.repository.TaskRepository
import com.kironillo.domain.result.DomainError
import com.kironillo.domain.result.DomainException
import com.kironillo.domain.result.DomainResult

class GetTopicsUseCase(private val repository: TaskRepository) {
    suspend operator fun invoke(): DomainResult<List<Topic>> = try {
        DomainResult.Success(repository.getTopics())
    } catch (e: DomainException) {
        DomainResult.Error(e.domainError)
    } catch (_: Exception) {
        DomainResult.Error(DomainError.Unknown)
    }
}
