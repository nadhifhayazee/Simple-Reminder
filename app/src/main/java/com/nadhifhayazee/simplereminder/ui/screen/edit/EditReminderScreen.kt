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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
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
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Outlined.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Deadline Date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(dateFormat.format(Date(reminder.deadline)), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

                // Time Picker Trigger
                OutlinedCard(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Outlined.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text("Deadline Time", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            Text(timeFormat.format(Date(reminder.deadline)), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }

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

        // Date Picker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = state.reminder?.deadline
            )
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // Maintain current time when changing date
                            val calendar = Calendar.getInstance().apply {
                                timeInMillis = state.reminder?.deadline ?: System.currentTimeMillis()
                                val currentHour = get(Calendar.HOUR_OF_DAY)
                                val currentMinute = get(Calendar.MINUTE)
                                
                                timeInMillis = millis
                                set(Calendar.HOUR_OF_DAY, currentHour)
                                set(Calendar.MINUTE, currentMinute)
                            }
                            viewModel.handleIntent(EditIntent.UpdateDeadline(calendar.timeInMillis))
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // Time Picker Dialog
        if (showTimePicker) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = state.reminder?.deadline ?: System.currentTimeMillis()
            }
            val timePickerState = rememberTimePickerState(
                initialHour = calendar.get(Calendar.HOUR_OF_DAY),
                initialMinute = calendar.get(Calendar.MINUTE)
            )
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        val newCalendar = Calendar.getInstance().apply {
                            timeInMillis = state.reminder?.deadline ?: System.currentTimeMillis()
                            set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            set(Calendar.MINUTE, timePickerState.minute)
                        }
                        viewModel.handleIntent(EditIntent.UpdateDeadline(newCalendar.timeInMillis))
                        showTimePicker = false
                    }) { Text("OK") }
                },
                text = {
                    TimePicker(state = timePickerState)
                }
            )
        }
    }
}
