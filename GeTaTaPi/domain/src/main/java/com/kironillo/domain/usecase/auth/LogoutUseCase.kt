package com.kironillo.domain.usecase.auth

import com.kironillo.domain.repository.AuthRepository

class LogoutUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}
