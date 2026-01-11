package com.dmd.tasky.features.agenda.data.remote

import com.dmd.tasky.features.agenda.data.remote.dto.EventDto
import com.dmd.tasky.features.agenda.data.remote.dto.ReminderDto
import com.dmd.tasky.features.agenda.data.remote.dto.TaskDto
import com.dmd.tasky.features.agenda.domain.model.AgendaItem

fun EventDto.toEvent(): AgendaItem.Event = AgendaItem.Event(
    id = this.id,
    title = this.title,
    description = this.description,
    from = this.from,
    to = this.to,
    remindAt = this.remindAt,
    updatedAt = this.updatedAt,
    attendeeIds = this.attendeeIds,
    photoKeys = this.photoKeys,
)

fun TaskDto.toTask(): AgendaItem.Task = AgendaItem.Task(
    id = this.id,
    title = this.title,
    description = this.description,
    remindAt = this.remindAt,
    updatedAt = this.updatedAt,
    time = this.time,
    isDone = this.isDone
)

fun ReminderDto.toReminder(): AgendaItem.Reminder = AgendaItem.Reminder(
    id = this.id,
    title = this.title,
    description = this.description,
    remindAt = this.remindAt,
    time = this.time,
    updatedAt = this.updatedAt,
)