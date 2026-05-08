package com.nadhifhayazee.simplereminder.ui.screen.edit.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nadhifhayazee.simplereminder.ui.theme.Spacing
import java.util.Calendar

@Composable
fun DayOfWeekPicker(
    selectedDays: List<Int>,
    onDaysSelected: (List<Int>) -> Unit
) {
    val days = listOf(
        "S" to Calendar.SUNDAY,
        "M" to Calendar.MONDAY,
        "T" to Calendar.TUESDAY,
        "W" to Calendar.WEDNESDAY,
        "T" to Calendar.THURSDAY,
        "F" to Calendar.FRIDAY,
        "S" to Calendar.SATURDAY
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        days.forEach { (label, dayValue) ->
            val isSelected = selectedDays.contains(dayValue)
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        val newList = if (isSelected) {
                            selectedDays.filter { it != dayValue }
                        } else {
                            selectedDays + dayValue
                        }
                        onDaysSelected(newList)
                    },
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
