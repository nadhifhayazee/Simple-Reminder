package com.nadhifhayazee.simplereminder.ui.screen.edit

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nadhifhayazee.simplereminder.ui.component.LoadingScreen
import com.nadhifhayazee.simplereminder.ui.screen.edit.components.DeadlineCard
import com.nadhifhayazee.simplereminder.ui.screen.edit.components.EditDatePickerDialog
import com.nadhifhayazee.simplereminder.ui.screen.edit.components.EditTimePickerDialog
import com.nadhifhayazee.simplereminder.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminderScreen(
    reminderId: Int,
    onNavigateBack: () -> Unit,
    viewModel: EditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
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
    ) { padding ->
        AnimatedContent(
            targetState = state,
            modifier = Modifier.padding(padding),
            label = "editContent"
        ) { targetState ->
            if (targetState.isLoading) {
                LoadingScreen()
            } else if (targetState.reminder != null) {
                val reminder = targetState.reminder!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                ) {
                    // Name Field - Premium Style
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        Text(
                            "What should be done?",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = reminder.name,
                            onValueChange = { viewModel.handleIntent(EditIntent.UpdateName(it)) },
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

                    // Deadline Section
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        Text(
                            "When?",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        DeadlineCard(
                            label = "Date",
                            value = dateFormat.format(Date(reminder.deadline)),
                            icon = Icons.Outlined.CalendarMonth,
                            onClick = { showDatePicker = true }
                        )

                        DeadlineCard(
                            label = "Time",
                            value = timeFormat.format(Date(reminder.deadline)),
                            icon = Icons.Outlined.Schedule,
                            onClick = { showTimePicker = true }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { viewModel.handleIntent(EditIntent.SaveReminder) },
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
