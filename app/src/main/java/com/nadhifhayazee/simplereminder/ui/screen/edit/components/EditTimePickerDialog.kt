package com.nadhifhayazee.simplereminder.ui.screen.edit.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTimePickerDialog(
    initialDateMillis: Long,
    onTimeSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = initialDateMillis
    }
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE)
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val newCalendar = Calendar.getInstance().apply {
                    timeInMillis = initialDateMillis
                    set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    set(Calendar.MINUTE, timePickerState.minute)
                }
                onTimeSelected(newCalendar.timeInMillis)
                onDismiss()
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}
