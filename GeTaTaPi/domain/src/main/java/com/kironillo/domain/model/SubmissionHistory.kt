package com.kironillo.domain.model

data class SubmissionHistoryItem(
    val id: String,
    val taskId: String,
    val storagePath: String,
    val status: String,
    val passed: Boolean?,
    val feedback: String?,
    val confidence: Double?,
    val createdAt: String,
    val checkedAt: String?,
)

data class SubmissionHistoryPage(
    val items: List<SubmissionHistoryItem>,
    val nextCursor: String?,
)
