package com.kironillo.domain.model

data class Statistics(
    val totalTasks: Int,
    val completedTasks: Int,
    val failedTasks: Int,
    val successRate: Float
)
