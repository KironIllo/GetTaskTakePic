package com.kironillo.domain.usecase.photo

import com.kironillo.domain.model.Answer
import com.kironillo.domain.repository.PhotoRepository
import com.kironillo.domain.result.DomainError
import com.kironillo.domain.result.DomainResult
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SavePhotoUseCaseTest {
    @Test
    fun `invoke returns success when photo is saved`() = runBlocking {
        val repository = FakePhotoRepository()
        val useCase = SavePhotoUseCase(repository)
        val result = useCase("photo.jpg", 55.75, 37.62)
        assertEquals(DomainResult.Success(Unit), result)
        assertEquals("photo.jpg", repository.savedPhotoPath)
    }
    @Test
    fun `invoke returns error when repository throws`() = runBlocking {
        val repository = FakePhotoRepository(shouldThrow = true)
        val useCase = SavePhotoUseCase(repository)
        val result = useCase("photo.jpg", null, null)
        assertTrue(result is DomainResult.Error)
        assertEquals(DomainError.Unknown, (result as DomainResult.Error).error)
    }
    private class FakePhotoRepository(
        private val shouldThrow: Boolean = false
    ): PhotoRepository{
        var savedPhotoPath: String? = null
        override suspend fun checkPhoto(topic: String, photoPath: String): Answer = Answer(false, null)
        override suspend fun savePhoto(photoPath: String, latitude: Double?, longitude: Double?) {
            if (shouldThrow) error("test error")
            savedPhotoPath = photoPath
        }
    }
}

