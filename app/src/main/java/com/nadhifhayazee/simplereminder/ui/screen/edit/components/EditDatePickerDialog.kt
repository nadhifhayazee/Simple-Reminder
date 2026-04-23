package com.nadhifhayazee.simplereminder.ui.screen.edit.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDatePickerDialog(
    initialDateMillis: Long?,
    onDateSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    // Maintain current time when changing date
                    val calendar = Calendar.getInstance().apply {
                        timeInMillis = initialDateMillis ?: System.currentTimeMillis()
                        val currentHour = get(Calendar.HOUR_OF_DAY)
                        val currentMinute = get(Calendar.MINUTE)
                        
                        timeInMillis = millis
                        set(Calendar.HOUR_OF_DAY, currentHour)
                        set(Calendar.MINUTE, currentMinute)
                    }
                    onDateSelected(calendar.timeInMillis)
                }
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
