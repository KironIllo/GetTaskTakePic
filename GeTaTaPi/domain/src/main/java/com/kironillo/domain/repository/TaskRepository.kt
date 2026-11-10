package com.kironillo.domain.repository

import com.kironillo.domain.model.Task
import com.kironillo.domain.model.TaskDifficulty

interface TaskRepository {
    suspend fun createTask(
        topic: String,
        difficulty: TaskDifficulty,
    ): Task

    suspend fun getTaskById(id: String): Task?

    suspend fun getTasks(): List<Task>
}
