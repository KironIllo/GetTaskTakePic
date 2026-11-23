package com.kironillo.domain.usecase.photo

import com.kironillo.domain.model.SubmissionResult
import com.kironillo.domain.repository.PhotoRepository

class GetSubmissionStatusUseCase(
    private val photoRepository: PhotoRepository,
) {
    suspend operator fun invoke(
        submissionId: String,
    ): SubmissionResult {
        return photoRepository.getSubmissionStatus(submissionId)
    }
}
