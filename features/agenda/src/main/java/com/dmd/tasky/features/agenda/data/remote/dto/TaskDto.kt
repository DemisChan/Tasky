package com.dmd.tasky.features.agenda.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TaskDto(
    val id: String,
    val title: String,
    val description: String,
    val remindAt: String,
    val updatedAt: String,
    val time: String,
    val isDone: Boolean,
)