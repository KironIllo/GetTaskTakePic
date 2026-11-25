package com.kironillo.data.mapper

import com.kironillo.data.remote.dto.SubmissionHistoryItemDto
import com.kironillo.data.remote.dto.SubmissionStatusDto
import com.kironillo.domain.model.SubmissionHistoryItem
import com.kironillo.domain.model.SubmissionResult

fun SubmissionStatusDto.toDomain(): SubmissionResult = SubmissionResult(
    id = id,
    status = status,
    passed = passed,
    feedback = feedback,
    confidence = confidence,
    checkedAt = checkedAt,
    latitude = latitude,
    longitude = longitude
)

fun SubmissionHistoryItemDto.toDomain(): SubmissionHistoryItem = SubmissionHistoryItem(
    id = id,
    taskId = taskId,
    storagePath = storagePath,
    status = status,
    passed = passed,
    feedback = feedback,
    confidence = confidence,
    createdAt = createdAt,
    checkedAt = checkedAt,
)
