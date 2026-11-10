package com.kironillo.domain.repository

import com.kironillo.domain.model.Answer

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
}
