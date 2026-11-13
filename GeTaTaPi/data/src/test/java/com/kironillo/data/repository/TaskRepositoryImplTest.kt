package com.kironillo.data.repository

import com.kironillo.data.local.database.TaskDao
import com.kironillo.data.local.entity.TaskEntity
import com.kironillo.data.remote.NetworkErrorHandler
import com.kironillo.data.remote.TaskApi
import com.kironillo.data.remote.connectivity.NetworkConnectivity
import com.kironillo.data.remote.dto.CreateTaskRequestDto
import com.kironillo.data.remote.dto.TaskDto
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.domain.result.DomainError
import com.kironillo.domain.result.DomainException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class TaskRepositoryImplTest {

    private val api = FakeTaskApi()
    private val taskDao = FakeTaskDao()
    private val networkConnectivity = FakeNetworkConnectivity()

    private val repository = TaskRepositoryImpl(
        taskApi = api,
        taskDao = taskDao,
        networkConnectivity = networkConnectivity,
        errorHandler = NetworkErrorHandler(networkConnectivity),
    )

    @Test
    fun `creates task and caches it`() = runBlocking {
        val task = repository.createTask("animals", TaskDifficulty.EASY)

        assertEquals("task-1", task.id)
        assertEquals(task, repository.getTaskById("task-1"))
        assertEquals(1, repository.getTasks().size)
    }

    @Test
    fun `returns domain error when server is unavailable and cache is empty`() {
        api.shouldFail = true
        networkConnectivity.connected = false
        val exception = assertThrows(DomainException::class.java) {
            runBlocking {
                repository.createTask("animals", TaskDifficulty.EASY)
            }
        }

        assertEquals(DomainError.Network, exception.domainError)
    }

    @Test
    fun `returns cached task when offline`() = runBlocking {
        val task = repository.createTask("animals", TaskDifficulty.EASY)

        networkConnectivity.connected = false

        val cachedTask = repository.getTaskById(task.id)

        assertEquals(task, cachedTask)
    }

    @Test
    fun `returns cached task when server fails`() = runBlocking {
        val task = repository.createTask("animals", TaskDifficulty.EASY)

        api.shouldFail = true

        val cachedTask = repository.getTaskById(task.id)

        assertEquals(task, cachedTask)
    }

    private class FakeTaskApi : TaskApi {
        var shouldFail = false

        override suspend fun createTask(
            request: CreateTaskRequestDto,
        ): TaskDto {
            if (shouldFail) throw IOException()
            return sampleTask()
        }

        override suspend fun getTask(id: String): TaskDto {
            if (shouldFail) throw IOException()
            return sampleTask()
        }

        private fun sampleTask() = TaskDto(
            id = "task-1",
            topicId = "animals",
            difficulty = "easy",
            prompt = "Find a cat",
            status = "active",
            createdAt = "2026-11-13T10:00:00Z",
            expiresAt = "2026-11-14T10:00:00Z",
        )
    }

    private class FakeTaskDao : TaskDao {
        private val tasks = mutableMapOf<String, TaskEntity>()

        override suspend fun insertTask(task: TaskEntity) {
            tasks[task.id] = task
        }

        override suspend fun insertTasks(tasks: List<TaskEntity>) {
            tasks.forEach { insertTask(it) }
        }

        override suspend fun getTaskById(id: String): TaskEntity? {
            return tasks[id]
        }

        override suspend fun getAllTasks(): List<TaskEntity> {
            return tasks.values.toList()
        }

        override suspend fun deleteTask(id: String) {
            tasks.remove(id)
        }

        override suspend fun clearTasks() {
            tasks.clear()
        }
    }

    private class FakeNetworkConnectivity : NetworkConnectivity {
        var connected = true

        override fun isConnected(): Boolean = connected
    }
}
