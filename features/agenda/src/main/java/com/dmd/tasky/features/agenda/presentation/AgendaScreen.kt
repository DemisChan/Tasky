package com.dmd.tasky.features.agenda.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun AgendaScreen(
    modifier: Modifier = Modifier,
    viewModel: AgendaViewModel = hiltViewModel(),
    onLogout: () -> Unit
) {
    val state = viewModel.state

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Agenda for ${state.selectedDate}")
    }
}


@Preview(showBackground = true, backgroundColor = 0xFF16161C)
@Composable
fun AgendaScreenPreview() {
    AgendaScreen(onLogout = {})
}