package com.kironillo.domain.usecase.progress

import com.kironillo.domain.model.Progress
import com.kironillo.domain.model.Statistics
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.domain.repository.ProgressRepository
import com.kironillo.domain.repository.StatisticsRepository
import com.kironillo.domain.result.DomainError
import com.kironillo.domain.result.DomainResult
import com.kironillo.domain.usecase.statistics.GetStatisticsUseCase
import com.kironillo.domain.usecase.statistics.StatisticsData
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

class GetStatisticsUseCaseTest {

    @Test
    fun `invoke returns progress`() = runBlocking {
        val expectedProgress = Progress(5, 10, 25, "2026-11-10")
        val repository = FakeProgressRepository(expectedProgress)
        val useCase = GetProgressUseCase(repository)
        val result = useCase()
        assertEquals(DomainResult.Success(expectedProgress), result)
    }

    @Test
    fun `invoke returns error when repository throws`() = runBlocking {
        val repository = FakeProgressRepository(shouldThrow = true)
        val useCase = GetProgressUseCase(repository)
        val result = useCase()
        assertTrue(result is DomainResult.Error)
        assertEquals(DomainError.Unknown, (result as DomainResult.Error).error)
    }

    private class FakeProgressRepository(
        private val progress: Progress = Progress(0, 0, 0, null),
        private val shouldThrow: Boolean = false
    ): ProgressRepository{
        override suspend fun getProgress(): Progress {
            if (shouldThrow) error("test error")
            return progress
        }

        override suspend fun updateStreak() = Unit
    }
}
