package com.kironillo.domain.model

enum class TaskDifficulty {
    EASY,
    MEDIUM,
    HARD
}

data class Task(
    val id: String,
    val topic: String,
    val difficulty: TaskDifficulty,
    val targetObject: String
)
