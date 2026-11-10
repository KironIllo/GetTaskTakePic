package com.kironillo.domain.usecase.statistics

import com.kironillo.domain.model.Statistics
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.domain.repository.StatisticsRepository
import com.kironillo.domain.result.DomainError
import com.kironillo.domain.result.DomainResult
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

class GetStatisticsUseCaseTest {

    @Test
    fun `invoke returns statistics`() = runBlocking {
        val statistics = Statistics(10, 8, 2, 0.8f)
        val repository = FakeStatisticsRepository(statistics)
        val useCase = GetStatisticsUseCase(repository)
        val result = useCase()
        assertEquals(DomainResult.Success(
            StatisticsData(
                mapOf("Nature" to statistics),
                mapOf(TaskDifficulty.EASY to statistics)
            )), result)
    }

    @Test
    fun `invoke returns error when repository throws`() = runBlocking {
        val repository = FakeStatisticsRepository(shouldThrow = true)
        val useCase = GetStatisticsUseCase(repository)
        val result = useCase()
        assertTrue(result is DomainResult.Error)
        assertEquals(DomainError.Unknown, (result as DomainResult.Error).error)
    }

    private class FakeStatisticsRepository(
        private val statistics: Statistics = Statistics(0, 0, 0, 0f),
        private val shouldThrow: Boolean = false
    ): StatisticsRepository{
        override suspend fun getStatisticsByTopics(): Map<String, Statistics> {
            if (shouldThrow) error("test error")
            return mapOf("Nature" to statistics)
        }

        override suspend fun getStatisticsByDifficulty(): Map<TaskDifficulty, Statistics> {
            if (shouldThrow) error("test error")
            return mapOf(TaskDifficulty.EASY to statistics)
        }
    }
}
