package com.dmd.tasky.features.agenda.data.remote

import com.dmd.tasky.features.agenda.data.remote.dto.*
import retrofit2.http.*

interface AgendaApi {
    // ========== AGENDA ==========
    @GET("agenda")
    suspend fun getAgenda(@Query("time") timestamp: Long): AgendaResponseDto

    @GET("fullAgenda")
    suspend fun getFullAgenda(): AgendaResponseDto

    @POST("syncAgenda")
    suspend fun syncAgenda(@Body request: SyncAgendaRequest)

    // ========== EVENTS ==========
    @POST("event")
    suspend fun createEvent(@Body request: CreateEventRequest): CreateEventResponse

    @PUT("event/{eventId}")
    suspend fun updateEvent(
        @Path("eventId") eventId: String,
        @Body request: UpdateEventRequest
    ): CreateEventResponse

    @POST("event/{eventId}/confirm-upload")
    suspend fun confirmUpload(
        @Path("eventId") eventId: String,
        @Body request: ConfirmUploadRequest
    ): CreateEventResponse


    @GET("event/{eventId}")
    suspend fun getEvent(@Path("eventId") eventId: String): EventDetailDto

    @DELETE("event")
    suspend fun deleteEvent(
        @Query("eventId") eventId: String,
        @Query("deleteAt") deleteAt: String? = null
    )

    // ========== ATTENDEES ==========
    @GET("attendee")
    suspend fun checkAttendee(@Query("email") email: String): AttendeeCheckResponse

    @DELETE("attendee")
    suspend fun removeAttendee(@Query("eventId") eventId: String)

    // ========== TASKS ==========
    @POST("task")
    suspend fun createTask(@Body request: CreateTaskRequest): TaskDto

    @PUT("task")
    suspend fun updateTask(@Body request: UpdateTaskRequest): TaskDto

    @GET("task/{taskId}")
    suspend fun getTask(@Path("taskId") taskId: String): TaskDto

    @DELETE("task/{taskId}")
    suspend fun deleteTask(
        @Path("taskId") taskId: String,
        @Query("deletedAt") deletedAt: String? = null
    )

    // ========== REMINDERS ==========
    @POST("reminder")
    suspend fun createReminder(@Body request: CreateReminderRequest): ReminderDto

    @PUT("reminder")
    suspend fun updateReminder(@Body request: UpdateReminderRequest): ReminderDto

    @GET("reminder/{reminderId}")
    suspend fun getReminder(@Path("reminderId") reminderId: String): ReminderDto

    @DELETE("reminder/{reminderId}")
    suspend fun deleteReminder(
        @Path("reminderId") reminderId: String,
        @Query("deletedAt") deletedAt: String? = null
    )
}