package com.dmd.tasky.features.agenda.domain.model

sealed interface AgendaItem {
    val id: String
    val title: String
    val description: String?
    val time: String?
    val remindAt: String

    data class Event(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val time: String? = null,
        override val remindAt: String,
        val updatedAt: String,
        val attendeeIds: List<String>,
        val from: String,
        val photoKeys: List<String>,
        val to: String,
    ) : AgendaItem

    data class Task(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val time: String,
        override val remindAt: String,
        val updatedAt: String,
        val isDone: Boolean,
    ) : AgendaItem

    data class Reminder(
        override val id: String,
        override val title: String,
        override val description: String?,
        override val time: String,
        override val remindAt: String,
        val updatedAt: String,
    ) : AgendaItem
}
