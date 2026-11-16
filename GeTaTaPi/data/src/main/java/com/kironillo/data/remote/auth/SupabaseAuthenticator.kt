package com.kironillo.data.remote.auth

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

@Singleton
class SupabaseAuthenticator @Inject constructor(
    private val authRepository: AuthRepositoryImpl,
    private val tokenProvider: TokenProvider,
) : Authenticator {

    private val lock = Any()

    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? {
        if (responseCount(response) >= 2) return null

        synchronized(lock) {
            val failedToken = response.request
                .header("Authorization")
                ?.removePrefix("Bearer ")

            val currentToken = tokenProvider.getAccessToken()

            // Another request may already have refreshed the token.
            if (!currentToken.isNullOrBlank() && currentToken != failedToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val refreshed = runBlocking {
                authRepository.refreshSession()
            }

            if (!refreshed) return null

            val newToken = tokenProvider.getAccessToken()
                ?.takeIf { it.isNotBlank() }
                ?: return null

            return response.request.newBuilder()
                .header("Authorization", "Bearer $newToken")
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var priorResponse = response.priorResponse

        while (priorResponse != null) {
            count++
            priorResponse = priorResponse.priorResponse
        }

        return count
    }
}
