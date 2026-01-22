package com.dmd.tasky.features.agenda.domain.repository

import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.features.agenda.domain.model.AgendaItem
import com.dmd.tasky.features.agenda.domain.model.Attendee
import java.time.LocalDate

/**
 * Type aliases for cleaner signatures.
 *
 * Design change: With composition, we no longer have AgendaItem.Event etc.
 * All methods now return AgendaItem (check details for type-specific data).
 */
typealias AgendaResult = Result<List<AgendaItem>, AgendaError>
typealias AgendaItemResult = Result<AgendaItem, AgendaError>
typealias LogoutResult = Result<Unit, AgendaError>
typealias AttendeeResult = Result<Attendee, AgendaError>

interface AgendaRepository {

    // ========== AGENDA OPERATIONS ==========
    suspend fun getAgendaForDate(date: LocalDate): AgendaResult
    suspend fun logout(): LogoutResult

    // ========== EVENT OPERATIONS ==========
    /**
     * Creates an event. The [item] MUST have details of type AgendaItemDetails.Event.
     * @param photos Compressed photo byte arrays to upload
     */
    suspend fun createEvent(item: AgendaItem, photos: List<ByteArray>): AgendaItemResult

    /**
     * Updates an event. The [item] MUST have details of type AgendaItemDetails.Event.
     * @param newPhotos New photos to add
     */
    suspend fun updateEvent(item: AgendaItem, newPhotos: List<ByteArray>): AgendaItemResult

    suspend fun deleteEvent(eventId: String): Result<Unit, AgendaError>
    suspend fun getEvent(eventId: String): AgendaItemResult

    // ========== TASK OPERATIONS ==========
    /**
     * Creates a task. The [item] MUST have details of type AgendaItemDetails.Task.
     */
    suspend fun createTask(item: AgendaItem): AgendaItemResult

    /**
     * Updates a task. The [item] MUST have details of type AgendaItemDetails.Task.
     */
    suspend fun updateTask(item: AgendaItem): AgendaItemResult

    suspend fun deleteTask(taskId: String): Result<Unit, AgendaError>
    suspend fun getTask(taskId: String): AgendaItemResult

    // ========== REMINDER OPERATIONS ==========
    /**
     * Creates a reminder. The [item] MUST have details of type AgendaItemDetails.Reminder.
     */
    suspend fun createReminder(item: AgendaItem): AgendaItemResult

    /**
     * Updates a reminder. The [item] MUST have details of type AgendaItemDetails.Reminder.
     */
    suspend fun updateReminder(item: AgendaItem): AgendaItemResult

    suspend fun deleteReminder(reminderId: String): Result<Unit, AgendaError>
    suspend fun getReminder(reminderId: String): AgendaItemResult

    // ========== ATTENDEE OPERATIONS ==========
    suspend fun checkAttendeeExists(email: String): AttendeeResult
    suspend fun removeAttendee(eventId: String): Result<Unit, AgendaError>
}
