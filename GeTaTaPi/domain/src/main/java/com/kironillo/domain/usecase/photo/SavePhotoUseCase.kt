package com.kironillo.domain.usecase.photo

import com.kironillo.domain.repository.PhotoRepository
import com.kironillo.domain.result.DomainError
import com.kironillo.domain.result.DomainException
import com.kironillo.domain.result.DomainResult

class SavePhotoUseCase(
    private val photoRepository: PhotoRepository
) {
    suspend operator fun invoke(
        photoPath: String,
        latitude: Double?,
        longtitude: Double?,
    ): DomainResult<Unit> {
        return try {
            photoRepository.savePhoto(photoPath, latitude, longtitude)
            DomainResult.Success(Unit)
        } catch (exception: DomainException) {
            DomainResult.Error(exception.domainError)
        } catch (_: Exception) {
            DomainResult.Error(DomainError.Unknown)
        }
    }
}
