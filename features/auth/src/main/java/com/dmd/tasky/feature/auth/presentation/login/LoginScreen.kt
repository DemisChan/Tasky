package com.dmd.tasky.feature.auth.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
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

@Composable
fun TaskyLoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val loginUiState = viewModel.state
    TaskyLoginContent(
        state = loginUiState,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@Composable
fun TaskyLoginContent(
    state: LoginUiState,
    onAction: (LoginAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    
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
                LoginInputField(
                    text = "Email",
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
                LoginInputField(
                    text = "Password",
                    value = state.password,
                    onValueChange = { onAction(LoginAction.PasswordChanged(it)) },
                    hidePassword = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp
                        )
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                )
                LoginButton(
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
                    text = annotatedString(onAction(LoginAction.SignUpClicked)),
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
fun LoginInputField(
    text: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hidePassword: VisualTransformation?,
    trailingIcon: (@Composable () -> Unit)?
) {
    OutlinedTextField(
        value = value,
        label = { Text(text) },
        maxLines = 1,
        visualTransformation = hidePassword?: VisualTransformation.None,
        onValueChange = onValueChange,
        trailingIcon = trailingIcon,
        modifier = modifier
    )
}

@Composable
fun LoginButton(
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

fun annotatedString(onAction: Unit) = buildAnnotatedString {
    append("DON’T HAVE AN ACCOUNT? ")

    val signUp = LinkAnnotation.Clickable(
        tag = "SIGN UP",
        linkInteractionListener = {onAction}
    )
    pushLink(signUp)

    withStyle(
        style = SpanStyle(
            color = Color.Blue,
            fontStyle = FontStyle.Italic
        )
    ) {
        append("SIGN UP")
    }
    pop()
}

@Preview(showBackground = false, backgroundColor = 0XFF16161C)
@Composable
fun TaskyLoginContentPreview() {
    TaskyLoginContent(
        state = LoginUiState(),
        onAction = {}
    )
}
