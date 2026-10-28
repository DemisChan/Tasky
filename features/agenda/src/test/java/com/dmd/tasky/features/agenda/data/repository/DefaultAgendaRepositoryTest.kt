package com.dmd.tasky.features.agenda.data.repository

import com.dmd.tasky.core.data.token.TokenManager
import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.features.agenda.data.remote.AgendaApi
import com.dmd.tasky.features.agenda.data.remote.dto.CreateEventResponse
import com.dmd.tasky.features.agenda.data.remote.dto.EventDetailDto
import com.dmd.tasky.features.agenda.domain.model.AgendaItem
import com.dmd.tasky.features.agenda.domain.model.AgendaItemDetails
import com.dmd.tasky.features.agenda.domain.repository.AgendaError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.time.LocalDateTime

class DefaultAgendaRepositoryTest {
    private lateinit var api: AgendaApi
    private lateinit var repository: DefaultAgendaRepository

    @Before
    fun setUp() {
        api = mockk()
        repository = DefaultAgendaRepository(api, mockk<TokenManager>(relaxed = true), mockk<OkHttpClient>())
    }

    private fun eventItem(id: String = "event-1") = AgendaItem(
        id = id,
        title = "Meeting",
        description = "desc",
        time = LocalDateTime.of(2026, 1, 15, 10, 0),
        remindAt = LocalDateTime.of(2026, 1, 15, 9, 30),
        updatedAt = LocalDateTime.of(2026, 1, 15, 9, 0),
        details = AgendaItemDetails.Event(
            to = LocalDateTime.of(2026, 1, 15, 11, 0),
            host = "host",
            isUserEventCreator = true,
            attendees = emptyList(),
            photos = emptyList()
        )
    )

    private fun eventResponse(id: String) = CreateEventResponse(
        event = EventDetailDto(
            id = id,
            title = "Meeting",
            description = "desc",
            from = "1768467600000",
            to = "1768471200000",
            remindAt = "1768465800000",
            hostId = "host",
            isUserEventCreator = true,
            attendees = emptyList(),
            photoKeys = emptyList()
        ),
        uploadUrls = emptyList()
    )

    @Test
    fun `updateEvent sends the event id to the api`() = runTest {
        coEvery { api.updateEvent("event-1", any()) } returns eventResponse("event-1")

        val result = repository.updateEvent(eventItem("event-1"), emptyList())

        assertTrue(result is Result.Success)
        coVerify { api.updateEvent("event-1", any()) }
    }

    @Test
    fun `updateEvent maps http 404 to NOT_FOUND`() = runTest {
        val http404 = HttpException(Response.error<Any>(404, "".toResponseBody()))
        coEvery { api.updateEvent(any(), any()) } throws http404

        val result = repository.updateEvent(eventItem(), emptyList())

        assertEquals(Result.Error(AgendaError.NOT_FOUND), result)
    }

    @Test
    fun `updateEvent with non event details returns VALIDATION_ERROR`() = runTest {
        val task = eventItem().copy(details = AgendaItemDetails.Task(isDone = false))

        val result = repository.updateEvent(task, emptyList())

        assertEquals(Result.Error(AgendaError.VALIDATION_ERROR), result)
    }
}
