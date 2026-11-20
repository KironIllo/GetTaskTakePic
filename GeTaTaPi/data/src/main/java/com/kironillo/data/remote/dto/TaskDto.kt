package com.kironillo.data.remote.dto

data class TaskDto(
    val id: String,
    val topicId: String,
    val topicTitle: String,
    val difficulty: String,
    val prompt: String,
    val status: String,
    val createdAt: String,
    val expiresAt: String?,
)

data class TaskListResponseDto(
    val data: List<TaskDto>,
)
