package com.kironillo.domain.usecase.task

import com.kironillo.domain.model.Task
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.domain.repository.TaskRepository
import com.kironillo.domain.result.DomainError
import com.kironillo.domain.result.DomainResult
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

class CreateTaskUseCaseTest {
    @Test
    fun `invoke returns created task`() = runBlocking {
        val expectedTask = Task(
            "1", "Nature", TaskDifficulty.EASY, "Tree"
        )
        val repository = FakeTaskRepository(expectedTask)
        val useCase = CreateTaskUseCase(repository)
        val result = useCase("Nature", TaskDifficulty.EASY)
        assertEquals(DomainResult.Success(expectedTask), result)
    }
    @Test
    fun `invoke returns error when repository throws`() = runBlocking {
        val repository = FakeTaskRepository(shouldThrow = true)
        val useCase = CreateTaskUseCase(repository)
        val result = useCase("Nature", TaskDifficulty.EASY)
        assertTrue(result is DomainResult.Error)
        assertEquals(DomainError.Unknown, (result as DomainResult.Error).error)
    }
}

private class FakeTaskRepository(
    private val taskToReturn : Task? = null,
    private val shouldThrow: Boolean = false
): TaskRepository{
    override suspend fun createTask(topic: String, difficulty: TaskDifficulty): Task {
        if (shouldThrow) error("test error")
        return requireNotNull(taskToReturn)
    }

    override suspend fun getTaskById(id: String): Task? = null

    override suspend fun getTasks(): List<Task> = emptyList()
}
