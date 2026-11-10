package com.kironillo.data.mapper

import com.kironillo.data.remote.dto.TaskDto
import com.kironillo.domain.model.TaskDifficulty
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskMapperTest {

    @Test
    fun `toDomain maps task dto correctly`() {
        val dto = TaskDto(
            id = "task-1",
            topicId = "nature",
            difficulty = "EASY",
            prompt = "Find a tree",
            status = "active",
            createdAt = "2026-11-10T10:00:00Z",
            expiresAt = "2026-11-10T11:00:00Z",
        )

        val result = dto.toDomain()

        assertEquals("task-1", result.id)
        assertEquals("nature", result.topic)
        assertEquals(TaskDifficulty.EASY, result.difficulty)
        assertEquals("Find a tree", result.targetObject)
    }
}
