package com.dmd.tasky.feature.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dmd.tasky.feature.auth.R
import com.dmd.tasky.feature.auth.presentation.register.RegisterAction
import com.dmd.tasky.feature.auth.presentation.register.RegisterUiState

@Composable
fun TaskyLoginScreen(
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val loginUiState = viewModel.state
    TaskyLoginContent(
        state = loginUiState,
        onAction = { action ->
            when (action) {
                is LoginAction.SignUpClicked -> onNavigateToRegister()
                else -> viewModel.onAction(action)
            }
        },
        modifier = modifier
    )
}

@Composable
private fun TaskyLoginContent(
    state: LoginUiState,
    onAction: (LoginAction) -> Unit,
    modifier: Modifier = Modifier
) {

    Scaffold { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = Color.Black),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.login_greeting),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(top = 70.dp)
                    .align(Alignment.CenterHorizontally),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(36.dp))

            Card(
                modifier = Modifier
                    .fillMaxSize(),
                shape = RoundedCornerShape(
                    topStart = 25.dp,
                    topEnd = 25.dp,
                ),
            ) {
                TaskyTextInputField(
                    hint = "Email",
                    value = state.email,
                    onValueChange = { onAction(LoginAction.EmailChanged(it)) },
                    modifier = Modifier
                        .padding(
                            top = 28.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    hidePassword = null,
                    trailingIcon = null
                )
                TaskyTextInputField(
                    hint = "Password",
                    value = state.password,
                    onValueChange = { onAction(LoginAction.PasswordChanged(it)) },
                    hidePassword = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp
                        )
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    trailingIcon = {
                        IconButton(onClick = { onAction(LoginAction.PasswordVisibilityChanged) }) {
                            Icon(
                                imageVector = if (state.passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (state.passwordVisible) {
                                    stringResource(R.string.content_description_hide_password)
                                } else {
                                    stringResource(R.string.content_description_show_password)
                                }
                            )
                        }
                    }
                )
                TaskyButton(
                    text = "LOG IN",
                    onClick = { onAction(LoginAction.LoginClicked) },
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
                        onAction = { onAction(LoginAction.SignUpClicked) },
                        state.javaClass.name
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .align(Alignment.CenterHorizontally),
                )

            }

        }

    }
}

@Composable
fun TaskyTextInputField(
    hint: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hidePassword: VisualTransformation?,
    trailingIcon: (@Composable () -> Unit)?
) {
    OutlinedTextField(
        value = value,
        label = { Text(hint) },
        maxLines = 1,
        visualTransformation = hidePassword ?: VisualTransformation.None,
        onValueChange = onValueChange,
        trailingIcon = trailingIcon,
        modifier = modifier
    )
}

@Composable
fun TaskyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        ),

        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontSize = 16.sp,
        )
    }

}

fun annotatedString(onAction: () -> Unit, state: String) = buildAnnotatedString {
    if (state == LoginUiState::class.qualifiedName) {
        append("DON'T HAVE AN ACCOUNT? ")
    }
    if (state == RegisterUiState::class.qualifiedName) {
        append("ALREADY HAVE AN ACCOUNT? ")
    }


    val signUp = LinkAnnotation.Clickable(
        tag = "SIGN UP",
        linkInteractionListener = { onAction() }
    )
    pushLink(signUp)

    withStyle(
        style = SpanStyle(
            color = Color.Blue,
            fontStyle = FontStyle.Italic
        )
    ) {
        if (state == LoginUiState::class.qualifiedName) {
            append("SIGN UP")
        }
        if (state == RegisterUiState::class.qualifiedName) {
            append("LOG IN")
        }
    }
    pop()
}

@Preview(showBackground = false, backgroundColor = 0XFF16161C)
@Composable
fun TaskyLoginContentPreview() {
    var state by remember { mutableStateOf(LoginUiState()) }

    TaskyLoginContent(
        state = state,
        onAction = { action ->
            when (action) {
                is LoginAction.EmailChanged -> {
                    state.copy(email = action.email)
                }

                is LoginAction.PasswordChanged -> {
                    state.copy(password = action.password)
                }

                is LoginAction.PasswordVisibilityChanged -> {
                    state.copy(passwordVisible = !state.passwordVisible)
                }

                is LoginAction.LoginClicked -> {}
                is LoginAction.SignUpClicked -> {}
            }
        }
    )
}
