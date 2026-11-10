package com.kironillo.domain.repository

interface AuthRepository {
    suspend fun getUserId(): String?
}
