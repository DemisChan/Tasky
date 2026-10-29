package com.dmd.tasky.features.agenda.data.repository

import com.dmd.tasky.core.data.token.TokenManager
import com.dmd.tasky.core.domain.util.Result
import com.dmd.tasky.features.agenda.data.remote.AgendaApi
import com.dmd.tasky.features.agenda.data.remote.dto.*
import com.dmd.tasky.features.agenda.data.remote.toAgendaItem
import com.dmd.tasky.features.agenda.data.remote.toAttendee
import com.dmd.tasky.features.agenda.domain.model.AgendaItem
import com.dmd.tasky.features.agenda.domain.model.AgendaItemDetails
import com.dmd.tasky.features.agenda.domain.model.Attendee
import com.dmd.tasky.features.agenda.domain.repository.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class DefaultAgendaRepository(
    private val api: AgendaApi,
    private val tokenManager: TokenManager,
    private val okHttpClient: OkHttpClient
) : AgendaRepository {

    // ========== HELPER METHODS ==========

    /**
     * Converts LocalDateTime to epoch milliseconds String for API calls.
     * Inverse of the toLocalDateTime() extension in Mappers.kt.
     */
    private fun LocalDateTime.toEpochMillisString(): String {
        return this.atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
            .toString()
    }

    /**
     * Wraps API calls with consistent error handling.
     * See earlier discussion on why this pattern is used.
     */
    private suspend fun <T> safeApiCall(
        operation: String,
        apiCall: suspend () -> T
    ): Result<T, AgendaError> {
        return try {
            Result.Success(apiCall())
        } catch (e: HttpException) {
            val code = e.code()
            Timber.e("HTTP Error during $operation: Code=$code")
            when (code) {
                400 -> Result.Error(AgendaError.VALIDATION_ERROR)
                401 -> Result.Error(AgendaError.UNAUTHORIZED)
                403 -> Result.Error(AgendaError.FORBIDDEN)
                404 -> Result.Error(AgendaError.NOT_FOUND)
                409 -> Result.Error(AgendaError.CONFLICT)
                in 500..599 -> Result.Error(AgendaError.SERVER_ERROR)
                else -> Result.Error(AgendaError.UNKNOWN)
            }
        } catch (e: SocketTimeoutException) {
            Timber.e("Timeout error during $operation")
            Result.Error(AgendaError.TIMEOUT)
        } catch (e: IOException) {
            Timber.e("Network error during $operation")
            Result.Error(AgendaError.NO_INTERNET)
        } catch (e: Exception) {
            Timber.e("Exception during $operation: ${e.message}")
            if (e is CancellationException) throw e
            Result.Error(AgendaError.UNKNOWN)
        }
    }

    private suspend fun uploadPhotoToS3(
        url: String,
        photoByteArray: ByteArray
    ): Result<Unit, AgendaError> {
        return safeApiCall("uploadPhotoToS3") {
            val requestBody = photoByteArray.toRequestBody("image/*".toMediaTypeOrNull())
            val request = Request.Builder()
                .url(url)
                .put(requestBody)
                .build()

            // execute() blocks; never run it on the caller's (possibly Main) dispatcher
            withContext(Dispatchers.IO) {
                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IOException("S3 upload failed with code: ${response.code}")
                    }
                }
            }
        }
    }

    // ========== AGENDA OPERATIONS ==========

    override suspend fun getAgendaForDate(date: LocalDate): AgendaResult {
        val timestamp = date.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val result = safeApiCall("getAgendaForDate") {
            api.getAgenda(timestamp)
        }

        return when (result) {
            is Result.Success -> {
                // Map all DTOs to unified AgendaItem
                val events = result.data.events.map { it.toAgendaItem() }
                val tasks = result.data.tasks.map { it.toAgendaItem() }
                val reminders = result.data.reminders.map { it.toAgendaItem() }

                // Combine and sort by time (common property - no type check needed!)
                Result.Success((events + tasks + reminders).sortedBy { it.time })
            }
            is Result.Error -> result
        }
    }

    override suspend fun logout(): LogoutResult {
        return safeApiCall("logout") {
            tokenManager.clearSession()
        }
    }

    // ========== EVENT OPERATIONS ==========

    override suspend fun createEvent(item: AgendaItem, photos: List<ByteArray>): AgendaItemResult {
        // Runtime check - caller must provide correct type
        val eventDetails = item.details as? AgendaItemDetails.Event
            ?: return Result.Error(AgendaError.VALIDATION_ERROR)

        val created = safeApiCall("createEvent") {
            val photoKeys = photos.mapIndexed { index, _ -> "photo$index" }
            val request = CreateEventRequest(
                id = item.id,
                title = item.title,
                description = item.description ?: "",
                from = item.time.toEpochMillisString(),           // Common 'time' = event 'from'
                to = eventDetails.to.toEpochMillisString(),       // Event-specific 'to'
                remindAt = item.remindAt.toEpochMillisString(),
                attendeeIds = eventDetails.attendees.map { it.userId },
                photoKeys = photoKeys,
                updatedAt = LocalDateTime.now().toEpochMillisString()
            )

            api.createEvent(request)
        }
        return when (created) {
            is Result.Error -> created
            is Result.Success -> uploadPhotosAndConfirm(item.id, created.data, photos)
        }
    }

    override suspend fun updateEvent(item: AgendaItem, newPhotos: List<ByteArray>): AgendaItemResult {
        val eventDetails = item.details as? AgendaItemDetails.Event
            ?: return Result.Error(AgendaError.VALIDATION_ERROR)

        val updated = safeApiCall("updateEvent") {
            val newPhotoKeys = newPhotos.mapIndexed { index, _ -> "photo$index" }

            val request = UpdateEventRequest(
                title = item.title,
                description = item.description ?: "",
                from = item.time.toEpochMillisString(),
                to = eventDetails.to.toEpochMillisString(),
                remindAt = item.remindAt.toEpochMillisString(),
                attendeeIds = eventDetails.attendees.map { it.userId },
                newPhotoKeys = newPhotoKeys,
                deletedPhotoKeys = emptyList(),
                isGoing = true,
                updatedAt = LocalDateTime.now().toEpochMillisString()
            )

            api.updateEvent(item.id, request)
        }
        return when (updated) {
            is Result.Error -> updated
            is Result.Success -> uploadPhotosAndConfirm(item.id, updated.data, newPhotos)
        }
    }

    /**
     * Phase 2 of the two-phase flow in the API doc: upload each photo to its pre-signed URL,
     * then confirm the uploaded keys so the server publishes download URLs.
     * Shared by create and update so both fail the same way: the first failed upload is returned.
     */
    private suspend fun uploadPhotosAndConfirm(
        eventId: String,
        response: CreateEventResponse,
        photos: List<ByteArray>
    ): AgendaItemResult {
        if (photos.isEmpty() || response.uploadUrls.isEmpty()) {
            return Result.Success(response.event.toAgendaItem())
        }

        for (upload in response.uploadUrls) {
            // Match by key ("photo0" -> index 0); the server may not return URLs in request order
            val photo = upload.photoKey.removePrefix("photo").toIntOrNull()?.let { photos.getOrNull(it) }
                ?: return Result.Error(AgendaError.VALIDATION_ERROR)
            val uploadResult = uploadPhotoToS3(upload.url, photo)
            if (uploadResult is Result.Error) return uploadResult
        }

        return safeApiCall("confirmUpload") {
            api.confirmUpload(
                eventId,
                ConfirmUploadRequest(uploadedKeys = response.uploadUrls.map { it.uploadKey })
            ).event.toAgendaItem()
        }
    }

    override suspend fun deleteEvent(eventId: String): Result<Unit, AgendaError> {
        return safeApiCall("deleteEvent") { api.deleteEvent(eventId) }
    }

    override suspend fun getEvent(eventId: String): AgendaItemResult {
        return safeApiCall("getEvent") {
            api.getEvent(eventId).toAgendaItem()
        }
    }

    // ========== TASK OPERATIONS ==========

    override suspend fun createTask(item: AgendaItem): AgendaItemResult {
        val taskDetails = item.details as? AgendaItemDetails.Task
            ?: return Result.Error(AgendaError.VALIDATION_ERROR)

        return safeApiCall("createTask") {
            val request = CreateTaskRequest(
                id = item.id,
                title = item.title,
                description = item.description ?: "",
                time = item.time.toEpochMillisString(),
                remindAt = item.remindAt.toEpochMillisString(),
                updatedAt = LocalDateTime.now().toEpochMillisString(),
                isDone = taskDetails.isDone
            )
            api.createTask(request).toAgendaItem()
        }
    }

    override suspend fun updateTask(item: AgendaItem): AgendaItemResult {
        val taskDetails = item.details as? AgendaItemDetails.Task
            ?: return Result.Error(AgendaError.VALIDATION_ERROR)

        return safeApiCall("updateTask") {
            val request = UpdateTaskRequest(
                id = item.id,
                title = item.title,
                description = item.description ?: "",
                time = item.time.toEpochMillisString(),
                remindAt = item.remindAt.toEpochMillisString(),
                updatedAt = LocalDateTime.now().toEpochMillisString(),
                isDone = taskDetails.isDone
            )
            api.updateTask(request).toAgendaItem()
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit, AgendaError> {
        return safeApiCall("deleteTask") { api.deleteTask(taskId) }
    }

    override suspend fun getTask(taskId: String): AgendaItemResult {
        return safeApiCall("getTask") {
            api.getTask(taskId).toAgendaItem()
        }
    }

    // ========== REMINDER OPERATIONS ==========

    override suspend fun createReminder(item: AgendaItem): AgendaItemResult {
        // Reminder has no unique fields, but validate it's the right type
        if (item.details !is AgendaItemDetails.Reminder) {
            return Result.Error(AgendaError.VALIDATION_ERROR)
        }

        return safeApiCall("createReminder") {
            val request = CreateReminderRequest(
                id = item.id,
                title = item.title,
                description = item.description ?: "",
                time = item.time.toEpochMillisString(),
                remindAt = item.remindAt.toEpochMillisString(),
                updatedAt = LocalDateTime.now().toEpochMillisString()
            )
            api.createReminder(request).toAgendaItem()
        }
    }

    override suspend fun updateReminder(item: AgendaItem): AgendaItemResult {
        if (item.details !is AgendaItemDetails.Reminder) {
            return Result.Error(AgendaError.VALIDATION_ERROR)
        }

        return safeApiCall("updateReminder") {
            val request = UpdateReminderRequest(
                id = item.id,
                title = item.title,
                description = item.description ?: "",
                time = item.time.toEpochMillisString(),
                remindAt = item.remindAt.toEpochMillisString(),
                updatedAt = LocalDateTime.now().toEpochMillisString()
            )
            api.updateReminder(request).toAgendaItem()
        }
    }

    override suspend fun deleteReminder(reminderId: String): Result<Unit, AgendaError> {
        return safeApiCall("deleteReminder") { api.deleteReminder(reminderId) }
    }

    override suspend fun getReminder(reminderId: String): AgendaItemResult {
        return safeApiCall("getReminder") {
            api.getReminder(reminderId).toAgendaItem()
        }
    }

    // ========== ATTENDEE OPERATIONS ==========

    override suspend fun checkAttendeeExists(email: String): AttendeeResult {
        return safeApiCall("checkAttendeeExists") {
            val response = api.checkAttendee(email)
            Attendee(
                email = response.email,
                username = response.fullName,
                userId = response.userId,
                eventId = "",
                isGoing = false,
                remindAt = ""
            )
        }
    }

    override suspend fun removeAttendee(eventId: String): Result<Unit, AgendaError> {
        return safeApiCall("removeAttendee") { api.removeAttendee(eventId) }
    }
}
