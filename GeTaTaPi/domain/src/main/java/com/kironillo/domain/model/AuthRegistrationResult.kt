package com.kironillo.domain.model

sealed interface AuthRegistrationResult {
    data class Authenticated(
        val userId: String,
    ) : AuthRegistrationResult

    data object EmailConfirmationRequired : AuthRegistrationResult
}
