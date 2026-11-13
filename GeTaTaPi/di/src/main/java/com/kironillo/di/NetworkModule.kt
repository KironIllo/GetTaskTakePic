package com.kironillo.di

import com.kironillo.data.remote.RetrofitClient
import com.kironillo.data.remote.TaskApi
import com.kironillo.data.remote.auth.TokenProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton
import com.kironillo.data.remote.NetworkErrorHandler
import com.kironillo.data.remote.connectivity.AndroidNetworkConnectivity
import com.kironillo.data.remote.connectivity.NetworkConnectivity

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideTokenProvider(): TokenProvider =
        TokenProvider { null }

    @Provides
    @Singleton
    fun provideRetrofit(
        tokenProvider: TokenProvider,
    ): Retrofit = RetrofitClient.create(
        baseUrl = BuildConfig.SUPABASE_URL.trimEnd('/') + "/functions/v1/api-v1/",
        tokenProvider = tokenProvider,
        isDebug = BuildConfig.DEBUG,
    )

    @Provides
    @Singleton
    fun provideTaskApi(retrofit: Retrofit): TaskApi =
        retrofit.create(TaskApi::class.java)

    @Provides
    @Singleton
    fun provideNetworkConnectivity(
        implementation: AndroidNetworkConnectivity,
    ): NetworkConnectivity = implementation

    @Provides
    @Singleton
    fun provideNetworkErrorHandler(
        connectivity: NetworkConnectivity,
    ): NetworkErrorHandler = NetworkErrorHandler(connectivity)
}
