package com.nadhifhayazee.simplereminder.ui.screen.edit

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nadhifhayazee.simplereminder.ui.component.LoadingScreen
import com.nadhifhayazee.simplereminder.ui.screen.edit.components.DeadlineCard
import com.nadhifhayazee.simplereminder.ui.screen.edit.components.EditDatePickerDialog
import com.nadhifhayazee.simplereminder.ui.screen.edit.components.EditTimePickerDialog
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
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(reminderId) {
        viewModel.handleIntent(EditIntent.LoadReminder(reminderId))
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Edit Reminder", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingScreen(modifier = Modifier.padding(padding))
        } else if (state.reminder != null) {
            val reminder = state.reminder!!
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(24.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Name Field
                OutlinedTextField(
                    value = reminder.name,
                    onValueChange = { viewModel.handleIntent(EditIntent.UpdateName(it)) },
                    label = { Text("Reminder Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                )

                // Date Picker Trigger
                DeadlineCard(
                    label = "Deadline Date",
                    value = dateFormat.format(Date(reminder.deadline)),
                    icon = Icons.Outlined.DateRange,
                    onClick = { showDatePicker = true }
                )

                // Time Picker Trigger
                DeadlineCard(
                    label = "Deadline Time",
                    value = timeFormat.format(Date(reminder.deadline)),
                    icon = Icons.Outlined.Schedule,
                    onClick = { showTimePicker = true }
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { viewModel.handleIntent(EditIntent.SaveReminder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Text("Save Changes", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        if (showDatePicker) {
            EditDatePickerDialog(
                initialDateMillis = state.reminder?.deadline,
                onDateSelected = { viewModel.handleIntent(EditIntent.UpdateDeadline(it)) },
                onDismiss = { showDatePicker = false }
            )
        }

        if (showTimePicker) {
            EditTimePickerDialog(
                initialDateMillis = state.reminder?.deadline ?: System.currentTimeMillis(),
                onTimeSelected = { viewModel.handleIntent(EditIntent.UpdateDeadline(it)) },
                onDismiss = { showTimePicker = false }
            )
        }
    }
}
