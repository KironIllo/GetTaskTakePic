package com.kironillo.domain.repository

import com.kironillo.domain.model.Answer
import com.kironillo.domain.model.SubmissionHistoryPage
import com.kironillo.domain.model.SubmissionResult

interface PhotoRepository {
    suspend fun checkPhoto(
        topic: String,
        photoPath: String,
    ): Answer

    suspend fun savePhoto(
        photoPath: String,
        latitude: Double?,
        longitude: Double?,
    )

    suspend fun submitPhoto(
        taskId: String,
        photoPath: String,
        idempotencyKey: String,
        latitude: Double? = null,
        longitude: Double? = null,
    ): String

    suspend fun getSubmissionStatus(
        submissionId: String,
    ): SubmissionResult

    suspend fun getPhotoUrl(storagePath: String): String

    suspend fun getSubmissionHistory(
        limit: Int = 20,
        cursor: String? = null,
    ): SubmissionHistoryPage
}
