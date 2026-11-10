package com.kironillo.data.mapper

import com.kironillo.data.remote.dto.TaskDto
import com.kironillo.domain.model.Task
import com.kironillo.domain.model.TaskDifficulty

fun TaskDto.toDomain(): Task {
    return Task(
        id = id,
        topic = topicId,
        difficulty = TaskDifficulty.valueOf(difficulty.uppercase()),
        targetObject = prompt,
    )
}
