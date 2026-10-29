package com.dmd.tasky.features.agenda.data.repository

import com.dmd.tasky.core.data.token.TokenManager
import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.features.agenda.domain.model.*
import com.dmd.tasky.features.agenda.domain.repository.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.*
import java.time.*
import java.util.UUID

/**
 * Stand-in for [DefaultAgendaRepository] used when BuildConfig.USE_FAKE_BACKEND is true
 * (the Tasky backend is no longer reliably reachable).
 *
 * Keeps agenda items in memory and applies the rules from tasky-api.pdf that matter to the UI:
 * - blank title -> VALIDATION_ERROR, event `from` must be before `to`, `remindAt` before `from`
 * - creating an existing id -> CONFLICT, updating/deleting/getting a missing id -> NOT_FOUND
 *
 * Photos are not stored: a byte array has no URL, so new photos get a placeholder entry with an
 * empty url. Data is lost when the process dies; a few items are seeded for today.
 */
class InMemoryAgendaRepository(
    private val tokenManager: TokenManager
) : AgendaRepository {

    private val mutex = Mutex()
    private val items = LinkedHashMap<String, AgendaItem>()

    private val knownUsers = mapOf(
        "alice@example.com" to Attendee("alice@example.com", "Alice Johnson", "user-alice", "", false, ""),
        "bob@example.com" to Attendee("bob@example.com", "Bob Smith", "user-bob", "", false, "")
    )

    init {
        seed()
    }

    // ========== AGENDA ==========

    override suspend fun getAgendaForDate(date: LocalDate): AgendaResult = withLatency {
        Result.Success(
            items.values
                .filter { it.time.toLocalDate() == date }
                .sortedBy { it.time }
        )
    }

    override suspend fun logout(): LogoutResult {
        delay(LATENCY_MS)
        tokenManager.clearSession()
        return Result.Success(Unit)
    }

    // ========== EVENTS ==========

    override suspend fun createEvent(item: AgendaItem, photos: List<ByteArray>): AgendaItemResult =
        create(withNewPhotos(item, photos), ::isEvent)

    override suspend fun updateEvent(item: AgendaItem, newPhotos: List<ByteArray>): AgendaItemResult =
        update(withNewPhotos(item, newPhotos), ::isEvent)

    override suspend fun deleteEvent(eventId: String) = delete(eventId, ::isEvent)

    override suspend fun getEvent(eventId: String) = get(eventId, ::isEvent)

    // ========== TASKS ==========

    override suspend fun createTask(item: AgendaItem) = create(item, ::isTask)

    override suspend fun updateTask(item: AgendaItem) = update(item, ::isTask)

    override suspend fun deleteTask(taskId: String) = delete(taskId, ::isTask)

    override suspend fun getTask(taskId: String) = get(taskId, ::isTask)

    // ========== REMINDERS ==========

    override suspend fun createReminder(item: AgendaItem) = create(item, ::isReminder)

    override suspend fun updateReminder(item: AgendaItem) = update(item, ::isReminder)

    override suspend fun deleteReminder(reminderId: String) = delete(reminderId, ::isReminder)

    override suspend fun getReminder(reminderId: String) = get(reminderId, ::isReminder)

    // ========== ATTENDEES ==========

    override suspend fun checkAttendeeExists(email: String): AttendeeResult {
        delay(LATENCY_MS)
        val key = email.trim().lowercase()
        if (!EMAIL_REGEX.matches(key)) return Result.Error(AgendaError.INVALID_EMAIL)
        val user = knownUsers[key] ?: return Result.Error(AgendaError.ATTENDEE_NOT_FOUND)
        return Result.Success(user)
    }

    override suspend fun removeAttendee(eventId: String): Result<Unit, AgendaError> = withLatency {
        if (items[eventId]?.details is AgendaItemDetails.Event) {
            Result.Success(Unit)
        } else {
            Result.Error(AgendaError.NOT_FOUND)
        }
    }

    // ========== HELPERS ==========

    private suspend fun <T> withLatency(block: () -> Result<T, AgendaError>): Result<T, AgendaError> {
        delay(LATENCY_MS)
        return mutex.withLock { block() }
    }

    private suspend fun create(
        item: AgendaItem,
        isExpectedKind: (AgendaItemDetails) -> Boolean
    ): AgendaItemResult = withLatency {
        when {
            !isValid(item, isExpectedKind) -> Result.Error(AgendaError.VALIDATION_ERROR)
            items.containsKey(item.id) -> Result.Error(AgendaError.CONFLICT)
            else -> {
                val stored = item.copy(updatedAt = LocalDateTime.now())
                items[item.id] = stored
                Result.Success(stored)
            }
        }
    }

    private suspend fun update(
        item: AgendaItem,
        isExpectedKind: (AgendaItemDetails) -> Boolean
    ): AgendaItemResult = withLatency {
        val existing = items[item.id]
        when {
            !isValid(item, isExpectedKind) -> Result.Error(AgendaError.VALIDATION_ERROR)
            existing == null || !isExpectedKind(existing.details) -> Result.Error(AgendaError.NOT_FOUND)
            else -> {
                val stored = item.copy(updatedAt = LocalDateTime.now())
                items[item.id] = stored
                Result.Success(stored)
            }
        }
    }

    private suspend fun delete(
        id: String,
        isExpectedKind: (AgendaItemDetails) -> Boolean
    ): Result<Unit, AgendaError> = withLatency {
        val existing = items[id]
        if (existing == null || !isExpectedKind(existing.details)) {
            Result.Error(AgendaError.NOT_FOUND)
        } else {
            items.remove(id)
            Result.Success(Unit)
        }
    }

    private suspend fun get(
        id: String,
        isExpectedKind: (AgendaItemDetails) -> Boolean
    ): AgendaItemResult = withLatency {
        val existing = items[id]
        if (existing == null || !isExpectedKind(existing.details)) {
            Result.Error(AgendaError.NOT_FOUND)
        } else {
            Result.Success(existing)
        }
    }

    private fun isValid(item: AgendaItem, isExpectedKind: (AgendaItemDetails) -> Boolean): Boolean {
        if (!isExpectedKind(item.details) || item.title.isBlank()) return false
        val details = item.details
        if (details is AgendaItemDetails.Event) {
            return item.time.isBefore(details.to) && !item.remindAt.isAfter(item.time)
        }
        return true
    }

    private fun withNewPhotos(item: AgendaItem, newPhotos: List<ByteArray>): AgendaItem {
        val details = item.details as? AgendaItemDetails.Event ?: return item
        if (newPhotos.isEmpty()) return item
        val added = newPhotos.map { Photo(key = UUID.randomUUID().toString(), url = "") }
        return item.copy(details = details.copy(photos = details.photos + added))
    }

    private fun isEvent(details: AgendaItemDetails) = details is AgendaItemDetails.Event
    private fun isTask(details: AgendaItemDetails) = details is AgendaItemDetails.Task
    private fun isReminder(details: AgendaItemDetails) = details is AgendaItemDetails.Reminder

    private fun seed() {
        val today = LocalDate.now()
        fun at(hour: Int, minute: Int = 0) = LocalDateTime.of(today, LocalTime.of(hour, minute))
        val now = LocalDateTime.now()

        val eventId = UUID.randomUUID().toString()
        val seeded = listOf(
            AgendaItem(
                id = eventId,
                title = "Team Meeting",
                description = "Weekly sync meeting",
                time = at(9),
                remindAt = at(8, 30),
                updatedAt = now,
                details = AgendaItemDetails.Event(
                    to = at(10),
                    host = SEED_HOST_ID,
                    isUserEventCreator = true,
                    attendees = listOf(knownUsers.getValue("alice@example.com").copy(eventId = eventId, isGoing = true)),
                    photos = emptyList()
                )
            ),
            AgendaItem(
                id = UUID.randomUUID().toString(),
                title = "Complete API documentation",
                description = "Write comprehensive API docs",
                time = at(12),
                remindAt = at(11, 45),
                updatedAt = now,
                details = AgendaItemDetails.Task(isDone = false)
            ),
            AgendaItem(
                id = UUID.randomUUID().toString(),
                title = "Doctor Appointment",
                description = "Annual checkup",
                time = at(15),
                remindAt = at(14, 30),
                updatedAt = now,
                details = AgendaItemDetails.Reminder
            )
        )
        seeded.forEach { items[it.id] = it }
    }

    private companion object {
        const val LATENCY_MS = 400L
        const val SEED_HOST_ID = "fake-host"
        val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
