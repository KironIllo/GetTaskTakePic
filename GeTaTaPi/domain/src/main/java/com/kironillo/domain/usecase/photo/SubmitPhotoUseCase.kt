package com.kironillo.domain.usecase.photo

import com.kironillo.domain.repository.PhotoRepository

class SubmitPhotoUseCase(
    private val photoRepository: PhotoRepository,
) {
    suspend operator fun invoke(
        taskId: String,
        photoPath: String,
        idempotencyKey: String,
    ): String {
        return photoRepository.submitPhoto(
            taskId = taskId,
            photoPath = photoPath,
            idempotencyKey = idempotencyKey,
        )
    }
}
