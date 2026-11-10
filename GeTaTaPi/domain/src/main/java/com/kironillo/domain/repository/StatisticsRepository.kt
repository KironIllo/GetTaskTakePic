package com.kironillo.domain.repository

import com.kironillo.domain.model.Statistics
import com.kironillo.domain.model.TaskDifficulty

interface StatisticsRepository {
    suspend fun getStatisticsByTopics(): Map<String, Statistics>

    suspend fun getStatisticsByDifficulty(): Map<TaskDifficulty, Statistics>
}
