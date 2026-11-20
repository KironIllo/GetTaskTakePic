package com.kironillo.data.repository

import com.kironillo.data.local.database.TaskDao
import com.kironillo.data.mapper.toDomain
import com.kironillo.data.mapper.toEntity
import com.kironillo.data.remote.TaskApi
import com.kironillo.data.remote.NetworkErrorHandler
import com.kironillo.data.remote.connectivity.NetworkConnectivity
import com.kironillo.data.remote.dto.CreateTaskRequestDto
import com.kironillo.domain.model.Task
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.domain.repository.TaskRepository
import com.kironillo.domain.result.DomainError
import com.kironillo.domain.result.DomainException
import kotlinx.coroutines.CancellationException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskApi: TaskApi,
    private val taskDao: TaskDao,
    private val errorHandler: NetworkErrorHandler,
    private val networkConnectivity: NetworkConnectivity,
) : TaskRepository {

    override suspend fun createTask(
        topic: String,
        difficulty: TaskDifficulty,
    ): Task {
        try {
            val task = taskApi.createTask(
                CreateTaskRequestDto(
                    topicId = topic,
                    difficulty = difficulty.name.lowercase(),
                ),
            ).toDomain()

            taskDao.insertTask(task.toEntity())
            return task
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            throw DomainException(errorHandler.mapError(exception))
        }
    }

    override suspend fun getTaskById(id: String): Task? {
        val cachedTask = taskDao.getTaskById(id)?.toDomain()

        if (!networkConnectivity.isConnected()) {
            return cachedTask ?: throw DomainException(DomainError.Network)
        }

        return try {
            val task = taskApi.getTask(id).toDomain()
            taskDao.insertTask(task.toEntity())
            task
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            cachedTask ?: throw DomainException(errorHandler.mapError(exception))
        }
    }

    override suspend fun getTasks(): List<Task> {
        val cachedTasks = taskDao.getAllTasks().map { it.toDomain() }

        if (!networkConnectivity.isConnected()) {
            return cachedTasks.ifEmpty { throw DomainException(DomainError.Network) }
        }

        return try {
            val remoteTasks = taskApi.getTasks().data.map { it.toDomain() }
            // The server is the source of truth: replace the local snapshot, including when empty.
            taskDao.replaceTasks(remoteTasks.map { it.toEntity() })
            remoteTasks
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            if (cachedTasks.isNotEmpty()) cachedTasks
            else throw DomainException(errorHandler.mapError(exception))
        }
    }
}
