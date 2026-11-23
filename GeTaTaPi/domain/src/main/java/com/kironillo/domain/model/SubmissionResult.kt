package com.kironillo.domain.model

data class SubmissionResult(
    val id: String,
    val status: String,
    val passed: Boolean?,
    val feedback: String?,
    val confidence: Double?,
    val checkedAt: String?,
)
