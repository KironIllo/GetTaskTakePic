package com.kironillo.data.remote.dto

data class SubmissionDto(
    val id: String,
    val taskId: String,
    val status: String,
    val passed: Boolean?,
    val feedback: String?,
    val confidence: Float?,
    val createdAt: String,
    val checkedAt: String?,
)
