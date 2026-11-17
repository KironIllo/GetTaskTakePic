package com.kironillo.domain.repository

import com.kironillo.domain.model.AuthRegistrationResult

interface AuthRepository {

    suspend fun register(
        email: String,
        password: String,
    ): AuthRegistrationResult

    suspend fun login(
        email: String,
        password: String,
    ): String

    suspend fun logout()

    suspend fun getUserId(): String?

    suspend fun restoreSession(): String?
}
