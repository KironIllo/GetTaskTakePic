package com.kironillo.data.remote.dto

data class AuthRequestDto(
    val email: String,
    val password: String,
)

data class RefreshTokenRequestDto(
    val refresh_token: String,
)
