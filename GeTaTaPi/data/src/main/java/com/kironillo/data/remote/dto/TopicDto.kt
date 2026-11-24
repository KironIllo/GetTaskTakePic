package com.kironillo.data.remote.dto

data class TopicDto(
    val id: String,
    val code: String,
    val title: String,
    val description: String? = null,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
)

data class TopicListResponseDto(val data: List<TopicDto>)
