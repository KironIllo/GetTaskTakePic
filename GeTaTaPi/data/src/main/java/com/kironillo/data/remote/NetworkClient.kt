package com.kironillo.data.remote

import com.kironillo.data.remote.auth.AuthInterceptor
import com.kironillo.data.remote.auth.TokenProvider
import com.kironillo.data.remote.logging.createNetworkLoggingInterceptor
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object NetworkClient {

    fun create(
        tokenProvider: TokenProvider,
        isDebug: Boolean,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .apply {
                if (isDebug) {
                    addInterceptor(createNetworkLoggingInterceptor())
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .build()
    }
}
