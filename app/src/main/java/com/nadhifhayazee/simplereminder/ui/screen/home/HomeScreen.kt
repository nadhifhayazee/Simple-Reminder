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
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.usecase.GroupedReminders
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
        topBar = { HomeTopBar() },
        bottomBar = {
            QuickAddReminderBar(
                onAddReminder = { name ->
                    viewModel.handleIntent(HomeIntent.AddReminder(name))
                },
                modifier = Modifier.imePadding()
            )
        }
    ) { padding ->
        HomeContent(
            padding = padding,
            state = state,
            onStatusChange = { reminder, newStatus ->
                viewModel.handleIntent(HomeIntent.UpdateReminder(reminder.copy(status = newStatus)))
            },
            onEditReminder = onEditReminder
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar() {
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
}

@Composable
private fun HomeContent(
    padding: PaddingValues,
    state: HomeState,
    onStatusChange: (Reminder, com.nadhifhayazee.simplereminder.domain.model.ReminderStatus) -> Unit,
    onEditReminder: (Int) -> Unit
) {
    val uiState = when {
        state.isLoading -> "loading"
        state.groupedReminders.let { it.today.isEmpty() && it.daily.isEmpty() && it.weekly.isEmpty() && it.monthly.isEmpty() && it.upcoming.isEmpty() } -> "empty"
        else -> "data"
    }

    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    ) {
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                fadeIn(animationSpec = tween(400)) togetherWith
                        fadeOut(animationSpec = tween(400))
            },
            label = "homeContent"
        ) { targetUiState ->
            when (targetUiState) {
                "loading" -> LoadingScreen()
                "empty" -> EmptyState(message = "All caught up!")
                "data" -> ReminderList(
                    groupedReminders = state.groupedReminders,
                    onStatusChange = onStatusChange,
                    onEditReminder = onEditReminder
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun ReminderList(
    groupedReminders: GroupedReminders,
    onStatusChange: (Reminder, com.nadhifhayazee.simplereminder.domain.model.ReminderStatus) -> Unit,
    onEditReminder: (Int) -> Unit
) {
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
        if (groupedReminders.today.isNotEmpty()) {
            stickyHeader { SectionHeader("Today") }
            items(items = groupedReminders.today, key = { "today_${it.id}" }) { reminder ->
                ReminderItem(
                    reminder = reminder,
                    onStatusChange = { onStatusChange(reminder, it) },
                    onClick = { onEditReminder(reminder.id) }
                )
            }
        }

        if (groupedReminders.daily.isNotEmpty()) {
            stickyHeader { SectionHeader("Daily") }
            items(items = groupedReminders.daily, key = { "daily_${it.id}" }) { reminder ->
                ReminderItem(
                    reminder = reminder,
                    onStatusChange = { onStatusChange(reminder, it) },
                    onClick = { onEditReminder(reminder.id) }
                )
            }
        }

        if (groupedReminders.weekly.isNotEmpty()) {
            stickyHeader { SectionHeader("Weekly") }
            items(items = groupedReminders.weekly, key = { "weekly_${it.id}" }) { reminder ->
                ReminderItem(
                    reminder = reminder,
                    onStatusChange = { onStatusChange(reminder, it) },
                    onClick = { onEditReminder(reminder.id) }
                )
            }
        }

        if (groupedReminders.monthly.isNotEmpty()) {
            stickyHeader { SectionHeader("Monthly") }
            items(items = groupedReminders.monthly, key = { "monthly_${it.id}" }) { reminder ->
                ReminderItem(
                    reminder = reminder,
                    onStatusChange = { onStatusChange(reminder, it) },
                    onClick = { onEditReminder(reminder.id) }
                )
            }
        }

        groupedReminders.upcoming.forEach { (date, reminders) ->
            stickyHeader { SectionHeader(date) }
            items(items = reminders, key = { "once_${it.id}" }) { reminder ->
                ReminderItem(
                    reminder = reminder,
                    onStatusChange = { onStatusChange(reminder, it) },
                    onClick = { onEditReminder(reminder.id) }
                )
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = Spacing.xs)
        )
    }
}