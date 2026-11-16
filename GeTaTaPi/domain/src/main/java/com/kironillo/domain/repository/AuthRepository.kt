package com.kironillo.domain.repository

interface AuthRepository {
    suspend fun register(email: String, password: String): String

    suspend fun login(email: String, password: String): String

    suspend fun logout()

    suspend fun getUserId(): String?
}
