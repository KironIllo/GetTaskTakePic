package com.kironillo.data.mapper

import com.kironillo.data.remote.dto.SubmissionDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubmissionMapperTest {

    @Test
    fun `toDomain maps successful submission`() {
        val dto = createDto(
            passed = true,
            feedback = "Correct answer",
        )

        val result = dto.toDomain()

        assertTrue(result.passed)
        assertEquals("Correct answer", result.details)
    }

    @Test
    fun `toDomain maps failed submission`() {
        val dto = createDto(
            passed = false,
            feedback = "Try again",
        )

        val result = dto.toDomain()

        assertFalse(result.passed)
        assertEquals("Try again", result.details)
    }

    @Test
    fun `toDomain treats null passed as false`() {
        val dto = createDto(
            passed = null,
            feedback = null,
        )

        val result = dto.toDomain()

        assertFalse(result.passed)
        assertEquals(null, result.details)
    }

    private fun createDto(
        passed: Boolean?,
        feedback: String?,
    ) = SubmissionDto(
        id = "submission-1",
        taskId = "task-1",
        status = "checked",
        passed = passed,
        feedback = feedback,
        confidence = 0.95f,
        createdAt = "2026-11-10T10:00:00Z",
        checkedAt = "2026-11-10T10:01:00Z",
    )
}
