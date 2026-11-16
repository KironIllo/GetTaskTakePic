package com.kironillo.data.remote.auth

import okhttp3.Interceptor
import okhttp3.Response

class SupabaseKeyInterceptor(
    private val apiKey: String,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .header("apikey", apiKey)
            .build()

        return chain.proceed(request)
    }
}
