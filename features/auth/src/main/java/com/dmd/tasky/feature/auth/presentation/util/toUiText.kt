package com.dmd.tasky.feature.auth.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dmd.tasky.core.domain.util.UiText
import com.dmd.tasky.core.domain.util.UiText.DynamicString
import com.dmd.tasky.core.domain.util.UiText.StringResource
import com.dmd.tasky.feature.auth.R
import com.dmd.tasky.feature.auth.domain.model.AuthError


fun AuthError.toUiText(): UiText {
    return when (this) {
        AuthError.Auth.INVALID_CREDENTIALS -> StringResource(R.string.error_invalid_credentials)
        AuthError.Auth.USER_ALREADY_EXISTS -> StringResource(R.string.error_email_already_exists)
        AuthError.Auth.VALIDATION_FAILED -> StringResource(R.string.error_validation_failed)

        AuthError.Network.NO_INTERNET -> StringResource(R.string.error_no_internet_connection)
        AuthError.Network.SERVER_ERROR -> StringResource(R.string.server_error)
        AuthError.Network.TIMEOUT -> StringResource(R.string.error_request_timed_out)
        AuthError.Network.UNKNOWN -> StringResource(R.string.unknown_network_error)
    }
}

@Composable
fun UiText.asString(): String {
    return when (this) {
        is DynamicString -> value
        is StringResource -> stringResource(resId, *args.toTypedArray())
    }
}