package com.kironillo.domain.model

data class Progress(
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCompletedTasks: Int,
    val lastActivityDate: String?
)
