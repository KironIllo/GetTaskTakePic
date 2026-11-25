package com.kironillo.data.remote.dto

data class CreateSubmissionRequestDto(
    val taskId: String,
    val storagePath: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

data class SubmissionResponseDto(
    val id: String,
    val taskId: String,
    val status: String,
    val createdAt: String,
    val statusUrl: String,
)

data class SubmissionHistoryResponseDto(
    val data: List<SubmissionHistoryItemDto>,
    val page: SubmissionHistoryPageDto,
)

data class SubmissionHistoryItemDto(
    val id: String,
    val taskId: String,
    val storagePath: String,
    val status: String,
    val passed: Boolean?,
    val feedback: String?,
    val confidence: Double?,
    val createdAt: String,
    val checkedAt: String?,
    val latitude: Double? = null,
    val longitude: Double? = null,
)

data class SubmissionHistoryPageDto(
    val limit: Int,
    val nextCursor: String?,
)

data class SubmissionStatusDto(
    val id: String,
    val taskId: String,
    val storagePath: String,
    val status: String,
    val passed: Boolean?,
    val feedback: String?,
    val confidence: Double?,
    val createdAt: String,
    val checkedAt: String?,
    val latitude: Double? = null,
    val longitude: Double? = null,
)
