package com.kironillo.data.remote.dto

data class AuthResponseDto(
    val access_token: String?,
    val refresh_token: String?,
    val token_type: String?,
    val expires_in: Long?,
    val user: AuthUserDto?,
    val id: String?,
    val email: String?,
) {
    val resolvedUserId: String?
        get() = user?.id ?: id
}

data class AuthUserDto(
    val id: String,
    val email: String?,
)
