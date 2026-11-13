package com.kironillo.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kironillo.data.remote.auth.TokenProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    fun create(
        baseUrl: String,
        tokenProvider: TokenProvider,
        isDebug: Boolean,
    ): Retrofit {
        require(baseUrl.endsWith("/")) {
            "https://lhkqqjrpqmgvzbiryugv.supabase.co/functions/v1/api-v1/"
        }

        val gson: Gson = GsonBuilder().create()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(NetworkClient.create(tokenProvider, isDebug))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
}
