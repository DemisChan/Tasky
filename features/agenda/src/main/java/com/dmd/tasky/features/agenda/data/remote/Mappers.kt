package com.dmd.tasky.features.agenda.data.remote

import com.dmd.tasky.features.agenda.data.remote.dto.*
import com.dmd.tasky.features.agenda.domain.model.AgendaItem
import com.dmd.tasky.features.agenda.domain.model.AgendaItemDetails
import com.dmd.tasky.features.agenda.domain.model.Attendee
import com.dmd.tasky.features.agenda.domain.model.Photo
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Parses a timestamp string (epoch millis) to LocalDateTime.
 *
 * Why a helper function?
 * - API returns timestamps as epoch milliseconds (String in DTOs)
 * - Domain model uses LocalDateTime for type safety and easy formatting
 * - Centralizes parsing logic in one place
 */
private fun String.toLocalDateTime(): LocalDateTime {
    val epochMillis = this.toLongOrNull() ?: 0L
    return LocalDateTime.ofInstant(
        Instant.ofEpochMilli(epochMillis),
        ZoneId.systemDefault()
    )
}

// ========== EVENT MAPPINGS ==========

/**
 * Maps EventDetailDto (from GET /event/{id}) to domain AgendaItem.
 * EventDetailDto has full attendee info, not just IDs.
 */
fun EventDetailDto.toAgendaItem(): AgendaItem = AgendaItem(
    id = this.id,
    title = this.title,
    description = this.description,
    time = this.from.toLocalDateTime(),       // 'from' becomes common 'time'
    remindAt = this.remindAt.toLocalDateTime(),
    updatedAt = LocalDateTime.now(),          // Not in DTO, use current time
    details = AgendaItemDetails.Event(
        to = this.to.toLocalDateTime(),
        host = this.hostId,                   // Could fetch username if needed
        isUserEventCreator = this.isUserEventCreator,
        attendees = this.attendees.map { it.toAttendee() },
        photos = this.photoKeys.map { it.toPhoto() }
    )
)

/**
 * Maps EventDto (from GET /agenda list) to domain AgendaItem.
 * EventDto only has attendee IDs, not full info.
 */
fun EventDto.toAgendaItem(): AgendaItem = AgendaItem(
    id = this.id,
    title = this.title,
    description = this.description,
    time = this.from.toLocalDateTime(),       // 'from' becomes common 'time'
    remindAt = this.remindAt.toLocalDateTime(),
    updatedAt = this.updatedAt.toLocalDateTime(),
    details = AgendaItemDetails.Event(
        to = this.to.toLocalDateTime(),
        host = "",                            // Not in list DTO
        isUserEventCreator = false,           // Not in list DTO, assume false
        attendees = this.attendeeIds.map { attendeeId ->
            // List endpoint only gives IDs, create partial Attendee
            Attendee(
                email = "",
                username = "",
                userId = attendeeId,
                eventId = this.id,
                isGoing = true,               // Assume going if in list
                remindAt = ""
            )
        },
        photos = this.photoKeys.map { key ->
            Photo(key = key, url = "")        // URL not in list DTO
        }
    )
)

// ========== TASK MAPPINGS ==========

fun TaskDto.toAgendaItem(): AgendaItem = AgendaItem(
    id = this.id,
    title = this.title,
    description = this.description,
    time = this.time.toLocalDateTime(),
    remindAt = this.remindAt.toLocalDateTime(),
    updatedAt = this.updatedAt.toLocalDateTime(),
    details = AgendaItemDetails.Task(
        isDone = this.isDone
    )
)

// ========== REMINDER MAPPINGS ==========

fun ReminderDto.toAgendaItem(): AgendaItem = AgendaItem(
    id = this.id,
    title = this.title,
    description = this.description,
    time = this.time.toLocalDateTime(),
    remindAt = this.remindAt.toLocalDateTime(),
    updatedAt = this.updatedAt.toLocalDateTime(),
    details = AgendaItemDetails.Reminder      // data object - no properties needed
)

// ========== ATTENDEE & PHOTO MAPPINGS ==========

fun AttendeeDto.toAttendee(): Attendee = Attendee(
    email = this.email,
    username = this.username,
    userId = this.userId,
    eventId = this.eventId,
    isGoing = this.isGoing,
    remindAt = this.remindAt
)

fun PhotoDto.toPhoto(): Photo = Photo(
    key = this.key,
    url = this.url
)
