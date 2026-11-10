package com.kironillo.domain.usecase.photo

import com.kironillo.domain.model.Answer
import com.kironillo.domain.repository.PhotoRepository
import com.kironillo.domain.result.DomainError
import com.kironillo.domain.result.DomainResult
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

class CheckPhotoUseCaseTest {
    @Test
    fun `invoke returns answer when photo is checked`() = runBlocking {
        val expectedAnswer = Answer(
            true, "Good"
        )
        val repository = FakePhotoRepository(expectedAnswer)
        val useCase = CheckPhotoUseCase(repository)
        val result = useCase("Nature", "photo.jpg")
        assertEquals(DomainResult.Success(expectedAnswer), result)
    }
    @Test
    fun `invoke returns error when repository throws`() = runBlocking {
        val repository = FakePhotoRepository(shouldThrow = true)
        val useCase = CheckPhotoUseCase(repository)
        val result = useCase("Nature", "photo.jpg")
        assertTrue(result is DomainResult.Error)
        assertEquals(DomainError.Unknown, (result as DomainResult.Error).error)
    }
    private class FakePhotoRepository(
        private val answerToReturn : Answer? = null,
        private val shouldThrow: Boolean = false
    ): PhotoRepository{
        override suspend fun checkPhoto(topic: String, photoPath: String): Answer {
            if (shouldThrow) error("test error")
            return requireNotNull(answerToReturn)
        }

        override suspend fun savePhoto(photoPath: String, latitude: Double?, longitude: Double?) = Unit
    }
}

