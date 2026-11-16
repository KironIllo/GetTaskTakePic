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
import com.kironillo.data.remote.auth.AuthApi
import com.kironillo.data.remote.auth.DataStoreTokenProvider
import com.kironillo.data.remote.auth.SupabaseAuthenticator
import com.kironillo.data.remote.auth.SupabaseKeyInterceptor
import com.kironillo.data.remote.connectivity.AndroidNetworkConnectivity
import com.kironillo.data.remote.connectivity.NetworkConnectivity
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideTokenProvider(
        implementation: DataStoreTokenProvider,
    ): TokenProvider = implementation

    @Provides
    @Singleton
    fun provideRetrofit(
        tokenProvider: TokenProvider,
        authenticator: Authenticator
    ): Retrofit = RetrofitClient.create(
        baseUrl = BuildConfig.SUPABASE_URL.trimEnd('/') + "/functions/v1/api-v1/",
        tokenProvider = tokenProvider,
        isDebug = BuildConfig.DEBUG,
        authenticator = authenticator
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

    @Provides
    @Singleton
    @Named("authRetrofit")
    fun provideAuthRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(
                SupabaseKeyInterceptor(BuildConfig.SUPABASE_ANON_KEY),
            )
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(
                BuildConfig.SUPABASE_URL.trimEnd('/') + "/auth/v1/",
            )
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(
        @Named("authRetrofit") retrofit: Retrofit,
    ): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideSupabaseAuthenticator(
        implementation: SupabaseAuthenticator,
    ): Authenticator = implementation
}
