package com.dmd.tasky.feature.auth.presentation.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dmd.tasky.feature.auth.R
import com.dmd.tasky.feature.auth.presentation.login.LoginInputField
import com.dmd.tasky.feature.auth.presentation.login.annotatedString


@Composable
fun TaskyRegisterScreen(
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val registerUiState = viewModel.state
    TaskyRegisterContent(
        state = registerUiState,
        onAction = viewModel::onAction,
        modifier = modifier
    )
}

@Composable
fun TaskyRegisterContent(
    state: RegisterUiState,
    onAction: (RegisterAction) -> Unit,
    modifier: Modifier = Modifier,
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
                text = stringResource(R.string.register_greeting),
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
                    text = "Full Name",
                    value = state.fullName,
                    onValueChange = { onAction(RegisterAction.FullNameChanged(it)) },
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
                    text = "Email",
                    value = state.email,
                    onValueChange = { onAction(RegisterAction.EmailChanged(it)) },
                    modifier = Modifier
                        .padding(
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
                    onValueChange = { onAction(RegisterAction.PasswordChanged(it)) },
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
                Button(
                    onClick = { onAction(RegisterAction.RegisterClicked) },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("REGISTER")
                    }
                }

//                LoginButton(
//                    text = "GET STARTED",
//                    onClick = { onAction(RegisterAction.RegisterClicked) },
//                    modifier = Modifier
//                        .padding(
//                            top = 32.dp,
//                            start = 16.dp,
//                            end = 16.dp
//                        )
//                        .fillMaxWidth()
//                        .align(Alignment.CenterHorizontally)
//                )

                BasicText(
                    text = annotatedString(
                        onAction(RegisterAction.LoginClicked),
                        state.javaClass.name
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .align(Alignment.CenterHorizontally),
                )

                if (state.error != null) {
                    Text(
                        text = "Error: ${state.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (state.registrationSuccess) {
                    Text(
                        text = "Registration Successful!",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
            }
        }
    }
}


@Preview(showBackground = false, backgroundColor = 0XFF16161C)
@Composable
fun TaskyRegisterContentPreview() {
    TaskyRegisterContent(
        state = RegisterUiState(),
        onAction = {}
    )
}