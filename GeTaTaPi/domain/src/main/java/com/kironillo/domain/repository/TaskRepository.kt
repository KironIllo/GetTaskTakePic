package com.kironillo.domain.repository

import com.kironillo.domain.model.Task
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.domain.model.Topic

interface TaskRepository {
    suspend fun createTask(
        topic: String,
        difficulty: TaskDifficulty,
    ): Task

    suspend fun getTaskById(id: String): Task?

    suspend fun getTasks(): List<Task>

    suspend fun getTopics(): List<Topic>
}
