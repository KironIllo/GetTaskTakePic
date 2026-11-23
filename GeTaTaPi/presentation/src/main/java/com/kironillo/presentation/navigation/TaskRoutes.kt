package com.kironillo.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data class TaskRoute(
    val taskId: String,
    val topic: String,
    val difficulty: String,
    val targetObject: String,
)

@Serializable
data class ReviewRoute(
    val taskId: String,
    val photoUri: String,
)
