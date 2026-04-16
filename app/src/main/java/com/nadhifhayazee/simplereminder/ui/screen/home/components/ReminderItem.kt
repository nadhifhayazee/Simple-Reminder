package com.nadhifhayazee.simplereminder.ui.screen.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReminderItem(
    reminder: Reminder,
    onStatusChange: (ReminderStatus) -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val deadlineStr = dateFormat.format(Date(reminder.deadline))

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = reminder.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(status = reminder.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Deadline: $deadlineStr",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReminderStatus.entries.forEach { status ->
                    FilterChip(
                        selected = reminder.status == status,
                        onClick = { onStatusChange(status) },
                        label = { Text(status.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: ReminderStatus) {
    val color = when (status) {
        ReminderStatus.TODO -> Color.Blue
        ReminderStatus.IN_PROGRESS -> Color(0xFFFFA500) // Orange
        ReminderStatus.DONE -> Color.Green
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
