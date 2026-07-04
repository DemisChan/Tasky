package com.dmd.tasky.features.agenda.domain.repository

import com.dmd.tasky.core.domain.util.Error

enum class AgendaError : Error {
    // Network errors
    NO_INTERNET,           // IOException
    TIMEOUT,               // SocketTimeoutException
    SERVER_ERROR,          // 500-599
    UNKNOWN,               // Unexpected

    // HTTP errors
    VALIDATION_ERROR,      // 400
    UNAUTHORIZED,          // 401
    FORBIDDEN,             // 403 (not creator)
    NOT_FOUND,             // 404
    CONFLICT,              // 409

    // App-specific errors
    PHOTO_TOO_LARGE,       // Photo > 1MB after compression
    INVALID_EMAIL,         // Invalid attendee email
    ATTENDEE_NOT_FOUND     // User doesn't exist
}
