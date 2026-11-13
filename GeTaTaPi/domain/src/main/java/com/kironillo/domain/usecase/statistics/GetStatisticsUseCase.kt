package com.kironillo.domain.usecase.statistics

import com.kironillo.domain.model.Statistics
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.domain.repository.StatisticsRepository
import com.kironillo.domain.result.DomainError
import com.kironillo.domain.result.DomainException
import com.kironillo.domain.result.DomainResult

data class StatisticsData(
    val byTopics: Map<String, Statistics>,
    val byDifficulty: Map<TaskDifficulty, Statistics>
)

class GetStatisticsUseCase(
    private val statisticsRepository: StatisticsRepository
) {
    suspend operator fun invoke(): DomainResult<StatisticsData> {
        return try {
            val byTopics = statisticsRepository.getStatisticsByTopics()
            val byDifficulty = statisticsRepository.getStatisticsByDifficulty()
            DomainResult.Success(
                StatisticsData(
                byTopics,
                byDifficulty,
                )
            )
        } catch (exception: DomainException) {
            DomainResult.Error(exception.domainError)
        } catch (_: Exception) {
            DomainResult.Error(DomainError.Unknown)
        }
    }
}
