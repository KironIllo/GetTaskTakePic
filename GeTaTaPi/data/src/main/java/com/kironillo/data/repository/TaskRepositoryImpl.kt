package com.kironillo.data.repository

import com.kironillo.data.mapper.toDomain
import com.kironillo.data.remote.TaskApi
import com.kironillo.data.remote.dto.CreateTaskRequestDto
import com.kironillo.domain.model.Task
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.domain.repository.TaskRepository
import javax.inject.Inject
import javax.inject.Singleton
import com.kironillo.data.remote.NetworkErrorHandler
import com.kironillo.domain.result.DomainException

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskApi: TaskApi,
    private val errorHandler: NetworkErrorHandler,
) : TaskRepository {

    private val taskCache = mutableMapOf<String, Task>()

    override suspend fun createTask(
        topic: String,
        difficulty: TaskDifficulty,
    ): Task {
        return try {
            taskApi.createTask(
                CreateTaskRequestDto(
                    topicId = topic,
                    difficulty = difficulty.name.lowercase(),
                ),
            ).toDomain().also {
                taskCache[it.id] = it
            }
        } catch (exception: Exception) {
            throw DomainException(errorHandler.mapError(exception))
        }
    }

    override suspend fun getTaskById(id: String): Task? {
        taskCache[id]?.let { return it }

        return try {
            taskApi.getTask(id).toDomain().also {
                taskCache[it.id] = it
            }
        } catch (exception: Exception) {
            throw DomainException(errorHandler.mapError(exception))
        }
    }

    override suspend fun getTasks(): List<Task> = taskCache.values.toList()
}
