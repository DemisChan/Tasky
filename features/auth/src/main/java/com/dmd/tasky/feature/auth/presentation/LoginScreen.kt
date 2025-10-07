package com.dmd.tasky.feature.auth.presentation

import android.R.attr.onClick
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmd.tasky.feature.auth.R

@Composable
fun TaskyLoginScreen(
//    viewModel: LoginViewModel
) {


}

@Composable
fun TaskyLoginContent(
    // state: LoginState,
    // onEvent: (LoginEvent) -> Unit,
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
                LoginInputField(
                    text = "Email",
                    modifier = Modifier
                        .padding(
                            top = 28.dp,
                            start = 16.dp,
                            end = 16.dp
                        )
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )
                LoginInputField(
                    text = "Password",
                    modifier = Modifier
                        .padding(
                            start = 16.dp,
                            end = 16.dp
                        )
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )
                LoginButton(
                    text = "LOG IN",
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
                    text = annotatedString,
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
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = "",
        label = { Text(text) },
        onValueChange = {},
        modifier = modifier
    )
}

@Composable
fun LoginButton(text: String, modifier: Modifier = Modifier) {
    TextButton(
        onClick = { /*TODO*/ },
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

val annotatedString = buildAnnotatedString {
    append("DON’T HAVE AN ACCOUNT? ")

    val signUp = LinkAnnotation.Clickable(
        tag = "SIGN UP",
        linkInteractionListener = {}
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
    TaskyLoginContent()
}
