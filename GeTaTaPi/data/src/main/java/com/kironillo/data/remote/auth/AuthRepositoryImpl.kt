package com.kironillo.data.remote.auth

import com.kironillo.data.local.datastore.AuthSessionDataStore
import com.kironillo.data.remote.dto.AuthRequestDto
import com.kironillo.data.remote.dto.AuthResponseDto
import com.kironillo.data.remote.dto.RefreshTokenRequestDto
import com.kironillo.domain.model.AuthRegistrationResult
import com.kironillo.domain.repository.AuthRepository
import retrofit2.HttpException
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
    ): AuthRegistrationResult {
        val response = authApi.register(
            AuthRequestDto(email, password),
        )

        val userId = response.resolvedUserId
            ?: throw IllegalStateException(
                "Supabase не вернул ID пользователя",
            )

        val accessToken = response.access_token
        val refreshToken = response.refresh_token

        if (accessToken == null && refreshToken == null) {
            return AuthRegistrationResult.EmailConfirmationRequired
        }

        if (accessToken == null || refreshToken == null) {
            throw IllegalStateException(
                "Supabase вернул неполную сессию",
            )
        }

        saveSession(response)

        return AuthRegistrationResult.Authenticated(userId)
    }

    override suspend fun login(
        email: String,
        password: String,
    ): String {
        val response = authApi.login(
            request = AuthRequestDto(email, password),
        )

        val userId = response.resolvedUserId
            ?: throw IllegalStateException(
                "Supabase не вернул ID пользователя",
            )

        if (response.access_token == null ||
            response.refresh_token == null
        ) {
            throw IllegalStateException(
                "Supabase не вернул полноценную сессию",
            )
        }

        saveSession(response)

        return userId
    }

    override suspend fun logout() {
        sessionDataStore.clearSession()
    }

    override suspend fun getUserId(): String? {
        return sessionDataStore.getUserId()
    }

    override suspend fun restoreSession(): String? {
        val userId = sessionDataStore.getUserId() ?: return null
        val accessToken = sessionDataStore.getAccessToken()
        val refreshToken = sessionDataStore.getRefreshToken()

        if (accessToken.isNullOrBlank() ||
            refreshToken.isNullOrBlank()
        ) {
            sessionDataStore.clearSession()
            return null
        }

        // Если токен истёк, сначала обновляем его.
        if (sessionDataStore.isTokenExpired()) {
            if (!refreshSession()) {
                sessionDataStore.clearSession()
                return null
            }
        }

        val currentAccessToken =
            sessionDataStore.getAccessToken()

        if (currentAccessToken.isNullOrBlank()) {
            sessionDataStore.clearSession()
            return null
        }

        return try {
            // Проверяем существование пользователя на сервере.
            val currentUser = authApi.getCurrentUser(
                "Bearer $currentAccessToken",
            )

            currentUser.id
        } catch (exception: HttpException) {
            if (exception.code() in listOf(401, 403, 404)) {
                sessionDataStore.clearSession()
                null
            } else {
                throw exception
            }
        }
    }

    private suspend fun saveSession(
        response: AuthResponseDto,
    ) {
        sessionDataStore.saveSession(
            accessToken = requireNotNull(response.access_token),
            refreshToken = requireNotNull(response.refresh_token),
            userId = response.resolvedUserId,
            expiresIn = response.expires_in,
        )
    }

    suspend fun refreshSession(): Boolean {
        val refreshToken = sessionDataStore.getRefreshToken()
            ?: return false

        return try {
            val response = authApi.refreshToken(
                request = RefreshTokenRequestDto(refreshToken),
            )

            val accessToken = response.access_token
                ?: return false

            val newRefreshToken = response.refresh_token
                ?: return false

            sessionDataStore.saveSession(
                accessToken = accessToken,
                refreshToken = newRefreshToken,
                userId = response.resolvedUserId
                    ?: sessionDataStore.getUserId(),
                expiresIn = response.expires_in,
            )

            true
        } catch (exception: HttpException) {
            if (exception.code() in listOf(400, 401, 403, 404)) {
                false
            } else {
                throw exception
            }
        }
    }
}
