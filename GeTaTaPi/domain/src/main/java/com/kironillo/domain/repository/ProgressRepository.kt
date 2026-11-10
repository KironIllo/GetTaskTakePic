package com.kironillo.domain.repository

import com.kironillo.domain.model.Progress

interface ProgressRepository {
    suspend fun getProgress(): Progress

    suspend fun updateStreak()
}
