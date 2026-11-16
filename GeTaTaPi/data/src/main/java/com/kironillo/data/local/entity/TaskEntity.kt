package com.kironillo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val topic: String,
    val difficulty: String,
    val targetObject: String,
)
