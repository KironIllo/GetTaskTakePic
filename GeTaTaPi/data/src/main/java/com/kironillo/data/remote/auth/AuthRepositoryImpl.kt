package com.kironillo.data.remote.auth

import com.kironillo.data.local.datastore.AuthSessionDataStore
import com.kironillo.data.remote.dto.AuthRequestDto
import com.kironillo.data.remote.dto.RefreshTokenRequestDto
import com.kironillo.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val sessionDataStore: AuthSessionDataStore,
) : AuthRepository {

    override suspend fun register(
        email: String,
        password: String,
    ): String {
        val response = authApi.register(
            AuthRequestDto(email, password),
        )

        val userId = response.user?.id
            ?: throw IllegalStateException("Supabase не вернул ID пользователя")

        saveSessionIfAvailable(response)

        return userId
    }

    override suspend fun login(
        email: String,
        password: String,
    ): String {
        val response = authApi.login(
            request = AuthRequestDto(email, password),
        )

        val userId = response.user?.id
            ?: throw IllegalStateException("Supabase не вернул ID пользователя")

        saveSessionIfAvailable(response)

        if (response.access_token == null) {
            throw IllegalStateException("Supabase не вернул access token")
        }

        return userId
    }

    override suspend fun logout() {
        sessionDataStore.clearSession()
    }

    override suspend fun getUserId(): String? =
        sessionDataStore.getUserId()

    private suspend fun saveSessionIfAvailable(
        response: com.kironillo.data.remote.dto.AuthResponseDto,
    ) {
        val accessToken = response.access_token ?: return
        val refreshToken = response.refresh_token ?: return

        sessionDataStore.saveSession(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = response.user?.id,
            expiresIn = response.expires_in
        )
    }
    suspend fun refreshSession(): Boolean {
        val refreshToken = sessionDataStore.getRefreshToken()
            ?: return false

        return try {
            val response = authApi.refreshToken(
                request = RefreshTokenRequestDto(refreshToken),
            )

            val accessToken = response.access_token ?: return false
            val newRefreshToken = response.refresh_token ?: return false

            sessionDataStore.saveSession(
                accessToken = accessToken,
                refreshToken = newRefreshToken,
                userId = response.user?.id ?: sessionDataStore.getUserId(),
                expiresIn = response.expires_in,
            )

            true
        } catch (_: Exception) {
            false
        }
    }
}
