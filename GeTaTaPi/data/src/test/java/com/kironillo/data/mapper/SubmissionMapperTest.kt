package com.kironillo.data.mapper

import com.kironillo.data.remote.dto.SubmissionHistoryItemDto
import com.kironillo.data.remote.dto.SubmissionStatusDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubmissionMapperTest {

    @Test
    fun `status dto maps successful submission`() {
        val result = createStatusDto(passed = true, feedback = "Correct answer").toDomain()

        assertTrue(result.passed == true)
        assertEquals("Correct answer", result.feedback)
        assertEquals("submission-1", result.id)
    }

    @Test
    fun `status dto preserves failed submission`() {
        val result = createStatusDto(passed = false, feedback = "Try again").toDomain()

        assertFalse(result.passed == true)
        assertEquals("Try again", result.feedback)
    }

    @Test
    fun `status dto preserves nullable result fields`() {
        val result = createStatusDto(passed = null, feedback = null).toDomain()

        assertEquals(null, result.passed)
        assertEquals(null, result.feedback)
    }

    @Test
    fun `history dto maps all fields`() {
        val dto = SubmissionHistoryItemDto(
            id = "submission-2",
            taskId = "task-2",
            storagePath = "user/task/photo.jpg",
            status = "checked",
            passed = true,
            feedback = "Good",
            confidence = 0.95,
            createdAt = "2026-11-10T10:00:00Z",
            checkedAt = "2026-11-10T10:01:00Z",
        )

        val result = dto.toDomain()

        assertEquals(dto.id, result.id)
        assertEquals(dto.taskId, result.taskId)
        assertEquals(dto.storagePath, result.storagePath)
        assertEquals(dto.status, result.status)
        assertEquals(dto.passed, result.passed)
        assertEquals(dto.feedback, result.feedback)
        assertEquals(dto.confidence, result.confidence)
        assertEquals(dto.createdAt, result.createdAt)
        assertEquals(dto.checkedAt, result.checkedAt)
    }

    private fun createStatusDto(
        passed: Boolean?,
        feedback: String?,
    ) = SubmissionStatusDto(
        id = "submission-1",
        taskId = "task-1",
        storagePath = "user/task/photo.jpg",
        status = "checked",
        passed = passed,
        feedback = feedback,
        confidence = 0.95,
        createdAt = "2026-11-10T10:00:00Z",
        checkedAt = "2026-11-10T10:01:00Z",
    )
}
