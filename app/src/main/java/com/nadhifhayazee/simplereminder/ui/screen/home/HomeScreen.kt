package com.nadhifhayazee.simplereminder.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nadhifhayazee.simplereminder.ui.component.EmptyState
import com.nadhifhayazee.simplereminder.ui.component.LoadingScreen
import com.nadhifhayazee.simplereminder.ui.screen.home.components.QuickAddReminderBar
import com.nadhifhayazee.simplereminder.ui.screen.home.components.ReminderItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEditReminder: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Simple Reminder") }
            )
        },
        bottomBar = {
            QuickAddReminderBar(
                onAddReminder = { name ->
                    viewModel.handleIntent(HomeIntent.AddReminder(name))
                },
                modifier = Modifier.imePadding()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                state.isLoading -> LoadingScreen()
                state.reminders.isEmpty() -> EmptyState(message = "No reminders yet")
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.reminders) { reminder ->
                            ReminderItem(
                                reminder = reminder,
                                onStatusChange = { newStatus ->
                                    viewModel.handleIntent(
                                        HomeIntent.UpdateReminder(
                                            reminder.copy(status = newStatus)
                                        )
                                    )
                                },
                                onClick = { onEditReminder(reminder.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
