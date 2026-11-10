package com.kironillo.data.remote

import com.kironillo.data.remote.dto.CreateTaskRequestDto
import com.kironillo.data.remote.dto.TaskDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface TaskApi {

    @POST("tasks")
    suspend fun createTask(
        @Body request: CreateTaskRequestDto,
    ): TaskDto

    @GET("tasks/{id}")
    suspend fun getTask(
        @Path("id") id: String,
    ): TaskDto
}
