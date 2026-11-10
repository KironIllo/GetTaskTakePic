package com.kironillo.data.mapper

import com.kironillo.data.remote.dto.SubmissionDto
import com.kironillo.domain.model.Answer

fun SubmissionDto.toDomain(): Answer {
    return Answer(
        passed = passed == true,
        details = feedback,
    )
}
