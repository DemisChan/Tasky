package com.dmd.tasky.features.agenda.domain.repository

import com.dmd.tasky.core.domain.util.Error

enum class AgendaError : Error {
    NETWORK_ERROR, // 403, 404, 500, 502, 503
    SERVER_ERROR, // 500, 502, 503
    UNKNOWN, // Unexpected exceptions
    NO_INTERNET,       // IOException
    TIMEOUT,           // SocketTimeoutException
    UNAUTHORIZED,      // 401
    NOT_FOUND,         // 404
}

