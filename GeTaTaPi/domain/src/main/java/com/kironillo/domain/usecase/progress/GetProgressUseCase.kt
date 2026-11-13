package com.kironillo.domain.usecase.progress

import com.kironillo.domain.model.Progress
import com.kironillo.domain.repository.ProgressRepository
import com.kironillo.domain.result.DomainError
import com.kironillo.domain.result.DomainException
import com.kironillo.domain.result.DomainResult

class GetProgressUseCase(
    private val progressRepository: ProgressRepository
){
    suspend operator fun invoke(): DomainResult<Progress>{
        return try{
            val progress = progressRepository.getProgress()
            DomainResult.Success(progress)
        } catch (exception: DomainException) {
            DomainResult.Error(exception.domainError)
        } catch (_: Exception) {
            DomainResult.Error(DomainError.Unknown)
        }
    }
}
