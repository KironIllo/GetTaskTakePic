package com.kironillo.data.remote

import com.kironillo.data.remote.dto.CreateSubmissionRequestDto
import com.kironillo.data.remote.dto.SubmissionHistoryResponseDto
import com.kironillo.data.remote.dto.SubmissionResponseDto
import com.kironillo.data.remote.dto.SubmissionStatusDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface SubmissionApi {

    @POST("submissions")
    suspend fun createSubmission(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: CreateSubmissionRequestDto,
    ): SubmissionResponseDto

    @GET("submissions/{id}")
    suspend fun getSubmission(
        @Path("id") submissionId: String,
    ): SubmissionStatusDto

    @GET("submissions")
    suspend fun getSubmissions(
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null,
    ): SubmissionHistoryResponseDto
}
