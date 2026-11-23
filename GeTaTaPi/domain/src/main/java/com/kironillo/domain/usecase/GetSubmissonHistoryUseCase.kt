package com.kironillo.domain.usecase

import com.kironillo.domain.model.SubmissionHistoryPage
import com.kironillo.domain.repository.PhotoRepository
import javax.inject.Inject

class GetSubmissionHistoryUseCase @Inject constructor(
    private val photoRepository: PhotoRepository,
) {
    suspend operator fun invoke(
        limit: Int = 20,
        cursor: String? = null,
    ): SubmissionHistoryPage {
        return photoRepository.getSubmissionHistory(limit, cursor)
    }
}
