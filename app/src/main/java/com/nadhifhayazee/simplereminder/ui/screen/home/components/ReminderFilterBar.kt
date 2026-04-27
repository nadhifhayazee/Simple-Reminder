package com.nadhifhayazee.simplereminder.ui.screen.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderFilterBar(
    selectedStatus: ReminderStatus?,
    dateFilterStartDate: Long?,
    dateFilterEndDate: Long?,
    onStatusFilterChanged: (ReminderStatus?) -> Unit,
    onDateFilterChanged: (Long?, Long?) -> Unit,
    onClearFilters: () -> Unit,
    hasActiveFilters: Boolean,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var pickingStartDate by remember { mutableStateOf(true) }
    var tempStartDate by remember { mutableStateOf<Long?>(dateFilterStartDate) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = selectedStatus == ReminderStatus.TODO,
            onClick = {
                onStatusFilterChanged(ReminderStatus.TODO)
            },
            label = { Text("To Do", style = MaterialTheme.typography.labelMedium) },
            leadingIcon = {
                if (selectedStatus == ReminderStatus.TODO) {
                    StatusFilterDot(status = ReminderStatus.TODO)
                }
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        FilterChip(
            selected = selectedStatus == ReminderStatus.IN_PROGRESS,
            onClick = {
                onStatusFilterChanged(ReminderStatus.IN_PROGRESS)
            },
            label = { Text("In Progress", style = MaterialTheme.typography.labelMedium) },
            leadingIcon = {
                if (selectedStatus == ReminderStatus.IN_PROGRESS) {
                    StatusFilterDot(status = ReminderStatus.IN_PROGRESS)
                }
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        AssistChip(
            onClick = {
                pickingStartDate = true
                tempStartDate = dateFilterStartDate
                showDatePicker = true
            },
            label = {
                Text(
                    text = formatDateRangeLabel(dateFilterStartDate, dateFilterEndDate),
                    style = MaterialTheme.typography.labelMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(AssistChipDefaults.IconSize)
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (dateFilterStartDate != null)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                labelColor = if (dateFilterStartDate != null)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        if (hasActiveFilters) {
            IconButton(
                onClick = onClearFilters,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterListOff,
                    contentDescription = "Clear filters",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = key(pickingStartDate) {
            rememberDatePickerState(
                initialSelectedDateMillis = if (pickingStartDate) tempStartDate else (dateFilterEndDate ?: tempStartDate)
            )
        }

        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
                tempStartDate = null
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selected = datePickerState.selectedDateMillis
                        if (pickingStartDate) {
                            tempStartDate = selected
                            if (selected != null) {
                                pickingStartDate = false
                            } else {
                                onDateFilterChanged(null, null)
                                showDatePicker = false
                                tempStartDate = null
                            }
                        } else {
                            onDateFilterChanged(tempStartDate, selected)
                            showDatePicker = false
                            tempStartDate = null
                        }
                    }
                ) {
                    Text(if (pickingStartDate) "Next" else "Apply")
                }
            },
            dismissButton = {
                if (!pickingStartDate) {
                    TextButton(
                        onClick = {
                            pickingStartDate = true
                        }
                    ) {
                        Text("Back")
                    }
                }
                TextButton(
                    onClick = {
                        showDatePicker = false
                        tempStartDate = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        if (pickingStartDate) "Select start date" else "Select end date",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp)
                    )
                }
            )
        }
    }
}

private fun formatDateRangeLabel(startDate: Long?, endDate: Long?): String {
    if (startDate == null) return "Date"
    val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
    val startStr = sdf.format(Date(startDate))
    val endStr = endDate?.let { sdf.format(Date(it)) } ?: "..."
    return "$startStr - $endStr"
}

@Composable
private fun StatusFilterDot(status: ReminderStatus) {
    val color = when (status) {
        ReminderStatus.TODO -> Color(0xFF4285F4)
        ReminderStatus.IN_PROGRESS -> Color(0xFFFBBC04)
        ReminderStatus.DONE -> Color(0xFF34A853)
    }
    Surface(
        modifier = Modifier.size(8.dp),
        shape = androidx.compose.foundation.shape.CircleShape,
        color = color
    ) {}
}
