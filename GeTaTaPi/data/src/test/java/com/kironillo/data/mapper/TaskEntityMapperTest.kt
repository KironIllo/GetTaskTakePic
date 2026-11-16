package com.kironillo.data.mapper

import com.kironillo.data.local.entity.TaskEntity
import com.kironillo.domain.model.Task
import com.kironillo.domain.model.TaskDifficulty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskEntityMapperTest {

    @Test
    fun `converts entity to domain`() {
        val entity = TaskEntity(
            id = "task-1",
            topic = "animals",
            difficulty = "EASY",
            targetObject = "Find a cat",
            cachedAt = 1000L,
        )

        val task = entity.toDomain()

        assertEquals("task-1", task.id)
        assertEquals("animals", task.topic)
        assertEquals(TaskDifficulty.EASY, task.difficulty)
        assertEquals("Find a cat", task.targetObject)
    }

    @Test
    fun `converts domain to entity`() {
        val task = Task(
            id = "task-1",
            topic = "animals",
            difficulty = TaskDifficulty.EASY,
            targetObject = "Find a cat",
        )

        val entity = task.toEntity()

        assertEquals(task.id, entity.id)
        assertEquals(task.topic, entity.topic)
        assertEquals(task.difficulty.name, entity.difficulty)
        assertEquals(task.targetObject, entity.targetObject)
        assertTrue(entity.cachedAt > 0L)
    }
}
