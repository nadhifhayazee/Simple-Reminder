package com.nadhifhayazee.simplereminder.ui.screen.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus
import com.nadhifhayazee.simplereminder.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.material.icons.outlined.Repeat
import com.nadhifhayazee.simplereminder.domain.model.RepeatInterval

@Composable
fun ReminderItem(
    reminder: Reminder,
    onStatusChange: (ReminderStatus) -> Unit,
    onClick: () -> Unit
) {
    val isRecurring = reminder.repeatInterval != RepeatInterval.NONE
    val formatPattern = if (isRecurring) "HH:mm" else "MMM dd, HH:mm"
    val dateFormat = SimpleDateFormat(formatPattern, Locale.getDefault())
    val deadlineStr = dateFormat.format(Date(reminder.deadline))
    var showMenu by remember { mutableStateOf(false) }

    val isOverdue = reminder.deadline < System.currentTimeMillis()

    val remainingDaysInfo = if (isRecurring) {
        val now = Calendar.getInstance()
        val deadlineCal = Calendar.getInstance().apply { timeInMillis = reminder.deadline }

        now.set(Calendar.HOUR_OF_DAY, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)

        val deadlineDate = deadlineCal.clone() as Calendar
        deadlineDate.set(Calendar.HOUR_OF_DAY, 0)
        deadlineDate.set(Calendar.MINUTE, 0)
        deadlineDate.set(Calendar.SECOND, 0)
        deadlineDate.set(Calendar.MILLISECOND, 0)

        val diffMillis = deadlineDate.timeInMillis - now.timeInMillis
        val diffDays = (diffMillis / (24 * 60 * 60 * 1000)).toInt()

        when {
            diffDays == 0 -> "Today"
            diffDays == 1 -> "Tomorrow"
            diffDays > 1 -> "In $diffDays days"
            else -> null
        }
    } else null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        color = if (isOverdue) MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(Spacing.md)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isOverdue) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(28.dp)
                        .background(MaterialTheme.colorScheme.tertiary, shape = RoundedCornerShape(1.5.dp))
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
            }

            StatusIndicator(
                status = reminder.status,
                onStatusClick = { showMenu = true }
            )

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = reminder.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (reminder.repeatInterval != RepeatInterval.NONE) {
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Icon(
                            imageVector = Icons.Outlined.Repeat,
                            contentDescription = "Recurring",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = if (remainingDaysInfo != null) "$remainingDaysInfo, $deadlineStr" else deadlineStr,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            Box {
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    ReminderStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = status.displayName,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            },
                            onClick = {
                                onStatusChange(status)
                                showMenu = false
                            },
                            leadingIcon = {
                                StatusCircle(status = status, size = 10.dp)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusIndicator(
    status: ReminderStatus,
    onStatusClick: () -> Unit
) {
    val color by animateColorAsState(
        targetValue = when (status) {
            ReminderStatus.TODO -> MaterialTheme.colorScheme.outline
            ReminderStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
            ReminderStatus.DONE -> Color(0xFF10B981)
        },
        label = "statusColor"
    )

    Surface(
        modifier = Modifier
            .size(24.dp)
            .clickable(onClick = onStatusClick),
        shape = CircleShape,
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(2.dp, color)
    ) {
        if (status == ReminderStatus.DONE) {
            Box(
                modifier = Modifier
                    .padding(4.dp)
                    .background(color, CircleShape)
            )
        }
    }
}

@Composable
private fun StatusCircle(status: ReminderStatus, size: Dp) {
    val color = when (status) {
        ReminderStatus.TODO -> MaterialTheme.colorScheme.outline
        ReminderStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        ReminderStatus.DONE -> Color(0xFF10B981)
    }
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = color
    ) {}
}
