package com.kironillo.presentation.viewmodel

import com.kironillo.domain.model.Task
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.domain.repository.TaskRepository
import com.kironillo.domain.result.DomainError
import com.kironillo.domain.result.DomainException
import com.kironillo.domain.usecase.task.GetTasksUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state is loading`() =
        runTest {
            val viewModel = createViewModel(emptyList())

            assertEquals(HomeUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun `shows content when tasks exist`() =
        runTest {
            val tasks =
                listOf(
                    Task(
                        id = "1",
                        topic = "Nature",
                        difficulty = TaskDifficulty.EASY,
                        targetObject = "Find a tree",
                    ),
                )
            val viewModel = createViewModel(tasks)

            advanceUntilIdle()

            assertEquals(HomeUiState.Content(tasks), viewModel.uiState.value)
        }

    @Test
    fun `shows empty when no tasks exist`() =
        runTest {
            val viewModel = createViewModel(emptyList())

            advanceUntilIdle()

            assertEquals(HomeUiState.Empty, viewModel.uiState.value)
        }

    @Test
    fun `shows error when repository fails`() =
        runTest {
            val viewModel =
                createViewModel(
                    tasks = emptyList(),
                    error = DomainException(DomainError.Network),
                )

            advanceUntilIdle()

            assertEquals(
                HomeUiState.Error(
                    "Не удалось загрузить задачи. Попробуйте ещё раз.",
                ),
                viewModel.uiState.value,
            )
        }

    private fun createViewModel(
        tasks: List<Task>,
        error: Exception? = null,
    ): HomeViewModel {
        val repository = FakeTaskRepository(tasks, error)
        val useCase = GetTasksUseCase(repository)
        return HomeViewModel(useCase)
    }

    private class FakeTaskRepository(
        private val tasks: List<Task>,
        private val error: Exception?,
    ) : TaskRepository {
        override suspend fun getTasks(): List<Task> {
            error?.let { throw it }
            return tasks
        }

        override suspend fun createTask(
            topic: String,
            difficulty: TaskDifficulty,
        ): Task = throw UnsupportedOperationException()

        override suspend fun getTaskById(id: String): Task? = throw UnsupportedOperationException()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
