package com.kironillo.data.remote

import com.kironillo.data.remote.dto.TopicListResponseDto
import retrofit2.http.GET

interface TopicApi {
    @GET("topics")
    suspend fun getTopics(): TopicListResponseDto
}
