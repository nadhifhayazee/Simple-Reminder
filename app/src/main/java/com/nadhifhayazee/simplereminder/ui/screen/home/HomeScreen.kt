package com.nadhifhayazee.simplereminder.ui.screen.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nadhifhayazee.simplereminder.ui.component.EmptyState
import com.nadhifhayazee.simplereminder.ui.component.LoadingScreen
import com.nadhifhayazee.simplereminder.ui.screen.home.components.QuickAddReminderBar
import com.nadhifhayazee.simplereminder.ui.screen.home.components.ReminderItem
import com.nadhifhayazee.simplereminder.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onEditReminder: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "Reminders",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Keep track of your tasks",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                windowInsets = WindowInsets.statusBars
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
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith
                            fadeOut(animationSpec = tween(400))
                },
                label = "homeContent"
            ) { targetState ->
                when {
                    targetState.isLoading -> LoadingScreen()
                    targetState.reminders.isEmpty() -> EmptyState(message = "All caught up!")
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = Spacing.md,
                                end = Spacing.md,
                                top = Spacing.sm,
                                bottom = Spacing.xxl + Spacing.xl // Space for QuickAddBar
                            ),
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            items(
                                items = targetState.reminders,
                                key = { it.id }
                            ) { reminder ->
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
}
