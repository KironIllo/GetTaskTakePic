package com.kironillo.domain.usecase.photo

import com.kironillo.domain.model.Answer
import com.kironillo.domain.repository.PhotoRepository
import com.kironillo.domain.result.DomainError
import com.kironillo.domain.result.DomainException
import com.kironillo.domain.result.DomainResult

class CheckPhotoUseCase(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(
        topic: String,
        photoPath: String,
    ): DomainResult<Answer> {
        return try {
            val answer = photoRepository.checkPhoto(topic, photoPath)
            DomainResult.Success(answer)
        } catch (exception: DomainException) {
            DomainResult.Error(exception.domainError)
        } catch (_: Exception) {
            DomainResult.Error(DomainError.Unknown)
        }
    }
}
