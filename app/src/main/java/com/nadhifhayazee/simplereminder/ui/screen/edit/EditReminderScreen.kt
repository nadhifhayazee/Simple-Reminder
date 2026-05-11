package com.nadhifhayazee.simplereminder.ui.screen.edit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.RepeatInterval
import com.nadhifhayazee.simplereminder.ui.component.LoadingScreen
import com.nadhifhayazee.simplereminder.ui.screen.edit.components.*
import com.nadhifhayazee.simplereminder.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderScreen(
    reminderId: Int,
    onNavigateBack: () -> Unit,
    viewModel: EditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(reminderId) {
        viewModel.handleIntent(EditIntent.LoadReminder(reminderId))
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is EditEffect.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is EditEffect.ShowSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { EditTopBar(onNavigateBack) }
    ) { padding ->
        EditContent(
            padding = padding,
            state = state,
            onIntent = viewModel::handleIntent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                "Edit Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
private fun EditContent(
    padding: PaddingValues,
    state: EditState,
    onIntent: (EditIntent) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.padding(padding)) {
        AnimatedContent(
            targetState = state.isLoading,
            label = "editContent"
        ) { loading ->
            if (loading) {
                LoadingScreen()
            } else if (state.reminder != null) {
                val reminder = state.reminder
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    ErrorBanner(state.error)

                    EditNameField(
                        name = reminder.name,
                        onNameChange = { onIntent(EditIntent.UpdateName(it)) }
                    )

                    RepeatSettings(
                        interval = reminder.repeatInterval,
                        days = reminder.repeatDays ?: emptyList(),
                        onIntervalChange = { onIntent(EditIntent.UpdateRepeatInterval(it)) },
                        onDaysChange = { onIntent(EditIntent.UpdateRepeatDays(it)) }
                    )

                    DeadlineSettings(
                        deadline = reminder.deadline,
                        interval = reminder.repeatInterval,
                        onDateClick = { showDatePicker = true },
                        onTimeClick = { showTimePicker = true }
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    SaveButton(onSave = { onIntent(EditIntent.SaveReminder) })
                }
            }
        }

        if (showDatePicker) {
            EditDatePickerDialog(
                initialDateMillis = state.reminder?.deadline,
                onDateSelected = { onIntent(EditIntent.UpdateDeadline(it)) },
                onDismiss = { showDatePicker = false }
            )
        }

        if (showTimePicker) {
            EditTimePickerDialog(
                initialDateMillis = state.reminder?.deadline ?: System.currentTimeMillis(),
                onTimeSelected = { onIntent(EditIntent.UpdateDeadline(it)) },
                onDismiss = { showTimePicker = false }
            )
        }
    }
}

@Composable
private fun ErrorBanner(error: String?) {
    AnimatedVisibility(
        visible = error != null,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = error ?: "",
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(Spacing.md)
            )
        }
    }
}

@Composable
private fun EditNameField(name: String, onNameChange: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(
            "What should be done?",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            )
        )
    }
}

@Composable
private fun RepeatSettings(
    interval: RepeatInterval,
    days: List<Int>,
    onIntervalChange: (RepeatInterval) -> Unit,
    onDaysChange: (List<Int>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            "Repeat",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            RepeatInterval.entries.forEachIndexed { index, item ->
                SegmentedButton(
                    selected = interval == item,
                    onClick = { onIntervalChange(item) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = RepeatInterval.entries.size)
                ) {
                    Text(item.displayName)
                }
            }
        }

        AnimatedVisibility(visible = interval == RepeatInterval.WEEKLY) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    "Repeat on",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DayOfWeekPicker(
                    selectedDays = days,
                    onDaysSelected = onDaysChange
                )
            }
        }

        AnimatedVisibility(visible = interval == RepeatInterval.MONTHLY) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                Text(
                    "Repeat on day of month",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                DayOfMonthPicker(
                    selectedDay = days.firstOrNull() ?: 1,
                    onDaySelected = { onDaysChange(listOf(it)) }
                )
            }
        }
    }
}

@Composable
private fun DeadlineSettings(
    deadline: Long,
    interval: RepeatInterval,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val isOnce = interval == RepeatInterval.NONE

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            if (isOnce) "When?" else "At what time?",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        
        if (isOnce) {
            DeadlineCard(
                label = "Date",
                value = dateFormat.format(Date(deadline)),
                icon = Icons.Outlined.CalendarMonth,
                onClick = onDateClick
            )
        }

        DeadlineCard(
            label = "Time",
            value = timeFormat.format(Date(deadline)),
            icon = Icons.Outlined.Schedule,
            onClick = onTimeClick
        )
    }
}

@Composable
private fun SaveButton(onSave: () -> Unit) {
    Button(
        onClick = onSave,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Text(
            "Save Changes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}