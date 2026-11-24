package com.kironillo.domain.usecase.task

import com.kironillo.domain.model.Task
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.domain.model.Topic
import com.kironillo.domain.repository.TaskRepository
import com.kironillo.domain.result.DomainError
import com.kironillo.domain.result.DomainException
import com.kironillo.domain.result.DomainResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetTasksUseCaseTest {

    @Test
    fun `returns tasks when repository succeeds`() = runBlocking {
        val tasks = listOf(
            Task(
                id = "1",
                topic = "Nature",
                difficulty = TaskDifficulty.EASY,
                targetObject = "Find a tree",
            ),
        )

        val useCase = GetTasksUseCase(FakeTaskRepository(tasks))

        val result = useCase()

        assertEquals(DomainResult.Success(tasks), result)
    }

    @Test
    fun `returns empty list when no tasks exist`() = runBlocking {
        val useCase = GetTasksUseCase(FakeTaskRepository(emptyList()))

        val result = useCase()

        assertEquals(DomainResult.Success(emptyList<Task>()), result)
    }

    @Test
    fun `returns domain error when repository fails`() = runBlocking {
        val useCase = GetTasksUseCase(
            FakeTaskRepository(
                error = DomainException(DomainError.Network),
            ),
        )

        val result = useCase()

        assertEquals(DomainResult.Error(DomainError.Network), result)
    }

    private class FakeTaskRepository(
        private val tasks: List<Task> = emptyList(),
        private val error: Exception? = null,
    ) : TaskRepository {

        override suspend fun getTopics(): List<Topic> = emptyList()

    override suspend fun getTasks(): List<Task> {
            error?.let { throw it }
            return tasks
        }

        override suspend fun createTask(
            topic: String,
            difficulty: TaskDifficulty,
        ): Task = throw UnsupportedOperationException()

        override suspend fun getTaskById(id: String): Task? =
            throw UnsupportedOperationException()
    }
}
