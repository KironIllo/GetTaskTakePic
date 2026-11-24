package com.kironillo.di

import com.kironillo.data.remote.RetrofitClient
import com.kironillo.data.remote.TaskApi
import com.kironillo.data.remote.TopicApi
import com.kironillo.data.remote.auth.TokenProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton
import com.kironillo.data.remote.NetworkErrorHandler
import com.kironillo.data.remote.StorageApi
import com.kironillo.data.remote.SubmissionApi
import com.kironillo.data.remote.auth.AuthApi
import com.kironillo.data.remote.auth.AuthInterceptor
import com.kironillo.data.remote.auth.DataStoreTokenProvider
import com.kironillo.data.remote.auth.SupabaseAuthenticator
import com.kironillo.data.remote.auth.SupabaseKeyInterceptor
import com.kironillo.data.remote.connectivity.AndroidNetworkConnectivity
import com.kironillo.data.remote.connectivity.NetworkConnectivity
import com.kironillo.data.repository.PhotoRepositoryImpl
import com.kironillo.domain.repository.PhotoRepository
import com.kironillo.domain.usecase.photo.GetSubmissionStatusUseCase
import com.kironillo.domain.usecase.photo.SubmitPhotoUseCase
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
    fun provideSubmitPhotoUseCase(
        photoRepository: PhotoRepository,
    ): SubmitPhotoUseCase = SubmitPhotoUseCase(photoRepository)

    @Provides
    @Singleton
    fun provideGetSubmissionStatusUseCase(
        photoRepository: PhotoRepository,
    ): GetSubmissionStatusUseCase =
        GetSubmissionStatusUseCase(photoRepository)

    @Provides
    @Singleton
    fun provideTaskApi(retrofit: Retrofit): TaskApi =
        retrofit.create(TaskApi::class.java)

    @Provides
    @Singleton
    fun provideTopicApi(retrofit: Retrofit): TopicApi =
        retrofit.create(TopicApi::class.java)

    @Provides
    @Singleton
    fun provideSubmissionApi(retrofit: Retrofit): SubmissionApi =
        retrofit.create(SubmissionApi::class.java)

    @Provides
    @Singleton
    fun providePhotoRepository(
        implementation: PhotoRepositoryImpl,
    ): PhotoRepository = implementation

    @Provides
    @Singleton
    @Named("supabaseUrl")
    fun provideSupabaseUrl(): String = BuildConfig.SUPABASE_URL

    @Provides
    @Singleton
    @Named("storageRetrofit")
    fun provideStorageRetrofit(
        tokenProvider: TokenProvider,
        authenticator: Authenticator,
    ): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(
                SupabaseKeyInterceptor(BuildConfig.SUPABASE_ANON_KEY),
            )
            .authenticator(authenticator)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL.trimEnd('/') + "/storage/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideStorageApi(
        @Named("storageRetrofit") retrofit: Retrofit,
    ): StorageApi = retrofit.create(StorageApi::class.java)

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
