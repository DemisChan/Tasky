package com.dmd.tasky.features.auth.presentation.register

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dmd.tasky.core.domain.util.UiText
import com.dmd.tasky.features.auth.R
import com.dmd.tasky.features.auth.presentation.login.TaskyButton
import com.dmd.tasky.features.auth.presentation.login.TaskyTextInputField
import com.dmd.tasky.features.auth.presentation.login.annotatedString
import com.dmd.tasky.features.auth.presentation.util.ObserveAsEvents
import com.dmd.tasky.features.auth.presentation.util.asString


@Composable
fun TaskyRegisterScreen(
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val registerUiState = viewModel.state
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is RegisterEvent.Error -> {
                Toast.makeText(
                    context, event.error.asString(context = context), Toast.LENGTH_LONG
                ).show()
            }

            is RegisterEvent.Success -> {
                Toast.makeText(
                    context, "Registration successful", Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    TaskyRegisterContent(
        state = registerUiState,
        onAction = { action ->
            when (action) {
                is RegisterAction.LoginClicked -> onNavigateToLogin()
                else -> viewModel.onAction(action)
            }
        },
        modifier = modifier
    )
}

@Composable
private fun TaskyRegisterContent(
    state: RegisterUiState,
    onAction: (RegisterAction) -> Unit,
    modifier: Modifier = Modifier,
) {

    Scaffold { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.register_greeting),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(vertical = 40.dp)
                    .align(Alignment.CenterHorizontally),
                color = Color.White
            )

            Card(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(
                    topStart = 25.dp,
                    topEnd = 25.dp,
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    TaskyTextInputField(
                        hint = "Full Name",
                        value = state.fullName,
                        onValueChange = { onAction(RegisterAction.FullNameChanged(it)) },
                        modifier = Modifier
                            .padding(
                                top = 28.dp,
                            )
                            .fillMaxWidth(),
                        hidePassword = null,
                        trailingIcon = null
                    )
                    TaskyTextInputField(
                        hint = "Email",
                        value = state.email,
                        onValueChange = { onAction(RegisterAction.EmailChanged(it)) },
                        modifier = Modifier
                            .fillMaxWidth(),
                        hidePassword = null,
                        trailingIcon = null
                    )

                    TaskyTextInputField(
                        hint = "Password",
                        value = state.password,
                        onValueChange = { onAction(RegisterAction.PasswordChanged(it)) },
                        hidePassword = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { onAction(RegisterAction.PasswordVisibilityChanged) }) {
                                Icon(
                                    imageVector = if (state.passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (state.passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        }
                    )
                    // TODO(Add enabled flag to button)
                    TaskyButton(
                        text = "GET STARTED",
                        onClick = { onAction(RegisterAction.RegisterClicked) },
                        modifier = Modifier
                            .padding(
                                top = 32.dp,
                                start = 16.dp,
                                end = 16.dp
                            )
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                    )
                    BasicText(
                        text = annotatedString(
                            onAction = { onAction(RegisterAction.LoginClicked) },
                            state.javaClass.name
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .align(Alignment.CenterHorizontally),
                    )
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.error?.let { error ->
                            Text(
                                text = error.asString(),
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = false, backgroundColor = 0XFF16161C)
@Composable
fun TaskyRegisterContentPreview() {

    var state by remember { mutableStateOf(RegisterUiState()) }

    TaskyRegisterContent(
        state = state,
        onAction = { action ->
            when (action) {
                is RegisterAction.FullNameChanged -> {
                    state.copy(fullName = action.fullName)
                }

                is RegisterAction.EmailChanged -> {
                    state.copy(email = action.email)
                }

                is RegisterAction.PasswordChanged -> {
                    state.copy(password = action.password)
                }

                is RegisterAction.PasswordVisibilityChanged -> {
                    state.copy(passwordVisible = !state.passwordVisible)
                }

                is RegisterAction.RegisterClicked -> {}
                is RegisterAction.LoginClicked -> {}

            }
        }
    )
}

@Preview(name = "Register Error State", showBackground = false, backgroundColor = 0XFF16161C)
@Composable
fun TaskyRegisterErrorPreview() {
    val errorState = RegisterUiState(
        fullName = "John Doe",
        email = "john@example.com",
        password = "secret",
        passwordVisible = false,
        error = UiText.StringResource(R.string.error_email_already_exists),
    )

    TaskyRegisterContent(
        state = errorState,
        onAction = {}
    )
}

