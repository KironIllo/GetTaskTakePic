package com.kironillo.data.mapper

import com.kironillo.data.remote.dto.TaskDto
import com.kironillo.domain.model.Task
import com.kironillo.domain.model.TaskDifficulty
import com.kironillo.data.local.entity.TaskEntity

fun TaskDto.toDomain(): Task {
    return Task(
        id = id,
        topic = topicId,
        difficulty = TaskDifficulty.valueOf(difficulty.uppercase()),
        targetObject = prompt,
    )
}


fun TaskEntity.toDomain(): Task = Task(
    id = id,
    topic = topic,
    difficulty = TaskDifficulty.valueOf(difficulty),
    targetObject = targetObject,
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    topic = topic,
    difficulty = difficulty.name,
    targetObject = targetObject,
)
