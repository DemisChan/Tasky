package com.dmd.tasky.features.agenda.data.repository

import com.dmd.tasky.core.data.token.TokenManager
import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.features.agenda.domain.model.AgendaItem
import com.dmd.tasky.features.agenda.domain.model.AgendaItemDetails
import com.dmd.tasky.features.agenda.domain.repository.AgendaError
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class InMemoryAgendaRepositoryTest {
    private lateinit var tokenManager: TokenManager
    private lateinit var repository: InMemoryAgendaRepository
    private val tomorrow: LocalDate = LocalDate.now().plusDays(1)

    @Before
    fun setUp() {
        tokenManager = mockk(relaxed = true)
        repository = InMemoryAgendaRepository(tokenManager)
    }

    private fun task(id: String = "task-1", title: String = "Task") = AgendaItem(
        id = id,
        title = title,
        description = null,
        time = tomorrow.atTime(12, 0),
        remindAt = tomorrow.atTime(11, 45),
        updatedAt = LocalDateTime.now(),
        details = AgendaItemDetails.Task(isDone = false)
    )

    private fun event(from: LocalDateTime, to: LocalDateTime) = AgendaItem(
        id = "event-1",
        title = "Event",
        description = null,
        time = from,
        remindAt = from.minusMinutes(30),
        updatedAt = LocalDateTime.now(),
        details = AgendaItemDetails.Event(to, "host", true, emptyList(), emptyList())
    )

    @Test
    fun `seeded items are returned for today sorted by time`() = runTest {
        val result = repository.getAgendaForDate(LocalDate.now())

        val items = (result as Result.Success).data
        assertEquals(3, items.size)
        assertEquals(items.sortedBy { it.time }, items)
    }

    @Test
    fun `created task shows up on its date`() = runTest {
        repository.createTask(task())

        val items = (repository.getAgendaForDate(tomorrow) as Result.Success).data
        assertEquals(listOf("task-1"), items.map { it.id })
    }

    @Test
    fun `creating the same id twice returns CONFLICT`() = runTest {
        repository.createTask(task())

        assertEquals(Result.Error(AgendaError.CONFLICT), repository.createTask(task()))
    }

    @Test
    fun `blank title returns VALIDATION_ERROR`() = runTest {
        assertEquals(Result.Error(AgendaError.VALIDATION_ERROR), repository.createTask(task(title = " ")))
    }

    @Test
    fun `event ending before it starts returns VALIDATION_ERROR`() = runTest {
        val start = tomorrow.atTime(10, 0)

        val result = repository.createEvent(event(from = start, to = start.minusHours(1)), emptyList())

        assertEquals(Result.Error(AgendaError.VALIDATION_ERROR), result)
    }

    @Test
    fun `creating a task through createReminder returns VALIDATION_ERROR`() = runTest {
        assertEquals(Result.Error(AgendaError.VALIDATION_ERROR), repository.createReminder(task()))
    }

    @Test
    fun `updating a missing task returns NOT_FOUND`() = runTest {
        assertEquals(Result.Error(AgendaError.NOT_FOUND), repository.updateTask(task("missing")))
    }

    @Test
    fun `update replaces the stored task`() = runTest {
        repository.createTask(task())

        repository.updateTask(task(title = "Renamed"))

        assertEquals("Renamed", (repository.getTask("task-1") as Result.Success).data.title)
    }

    @Test
    fun `deleted task can no longer be fetched`() = runTest {
        repository.createTask(task())

        assertTrue(repository.deleteTask("task-1") is Result.Success)
        assertEquals(Result.Error(AgendaError.NOT_FOUND), repository.getTask("task-1"))
    }

    @Test
    fun `new photos are appended to the event`() = runTest {
        val start = tomorrow.atTime(10, 0)

        val result = repository.createEvent(event(start, start.plusHours(1)), listOf(ByteArray(1), ByteArray(1)))

        val details = (result as Result.Success).data.details as AgendaItemDetails.Event
        assertEquals(2, details.photos.size)
    }

    @Test
    fun `unknown attendee returns ATTENDEE_NOT_FOUND and bad email INVALID_EMAIL`() = runTest {
        assertEquals(Result.Error(AgendaError.ATTENDEE_NOT_FOUND), repository.checkAttendeeExists("nobody@example.com"))
        assertEquals(Result.Error(AgendaError.INVALID_EMAIL), repository.checkAttendeeExists("not-an-email"))
        assertTrue(repository.checkAttendeeExists("alice@example.com") is Result.Success)
    }

    @Test
    fun `logout clears the session`() = runTest {
        repository.logout()

        coVerify { tokenManager.clearSession() }
    }
}
