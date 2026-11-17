package com.kironillo.domain.usecase.auth

import com.kironillo.domain.model.AuthRegistrationResult
import com.kironillo.domain.repository.AuthRepository

class RegisterUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): AuthRegistrationResult {
        return repository.register(email, password)
    }
}
