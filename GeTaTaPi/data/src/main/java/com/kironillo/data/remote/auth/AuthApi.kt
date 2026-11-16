package com.kironillo.data.remote.auth

import com.kironillo.data.remote.dto.AuthRequestDto
import com.kironillo.data.remote.dto.AuthResponseDto
import com.kironillo.data.remote.dto.RefreshTokenRequestDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {

    @POST("signup")
    suspend fun register(
        @Body request: AuthRequestDto,
    ): AuthResponseDto

    @POST("token")
    suspend fun login(
        @Query("grant_type") grantType: String = "password",
        @Body request: AuthRequestDto,
    ): AuthResponseDto

    @POST("token")
    suspend fun refreshToken(
        @Query("grant_type") grantType: String = "refresh_token",
        @Body request: RefreshTokenRequestDto,
    ): AuthResponseDto
}
