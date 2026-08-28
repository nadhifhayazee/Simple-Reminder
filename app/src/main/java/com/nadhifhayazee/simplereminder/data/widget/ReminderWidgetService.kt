package com.nadhifhayazee.simplereminder.data.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.nadhifhayazee.simplereminder.R
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus
import com.nadhifhayazee.simplereminder.domain.model.RepeatInterval
import com.nadhifhayazee.simplereminder.domain.repository.ReminderRepository
import com.nadhifhayazee.simplereminder.domain.usecase.GetGroupedRemindersUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.*

class ReminderWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ReminderRemoteViewsFactory(this.applicationContext)
    }
}

sealed class WidgetListItem {
    data class Header(val title: String) : WidgetListItem()
    data class ReminderItem(val reminder: Reminder) : WidgetListItem()
}

class ReminderRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun repository(): ReminderRepository
        fun getGroupedRemindersUseCase(): GetGroupedRemindersUseCase
    }

    private lateinit var repository: ReminderRepository
    private lateinit var getGroupedRemindersUseCase: GetGroupedRemindersUseCase
    private var widgetItems: List<WidgetListItem> = emptyList()

    override fun onCreate() {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
            repository = entryPoint.repository()
            getGroupedRemindersUseCase = entryPoint.getGroupedRemindersUseCase()
        } catch (e: Exception) {
            Log.e("Widget", "Failed to get dependencies", e)
        }
    }

    override fun onDataSetChanged() {
        try {
            runBlocking {
                withTimeoutOrNull(3000L) {
                    val allReminders = repository.getReminders().first()
                    val grouped = getGroupedRemindersUseCase(allReminders)
                    widgetItems = mapToWidgetItems(grouped)
                } ?: Log.e("Widget", "Timed out waiting for reminders")
            }
        } catch (e: Exception) {
            Log.e("Widget", "Error fetching reminders", e)
        }
    }

    private fun mapToWidgetItems(grouped: com.nadhifhayazee.simplereminder.domain.usecase.GroupedReminders): List<WidgetListItem> {
        val result = mutableListOf<WidgetListItem>()
        
        if (grouped.today.isNotEmpty()) {
            result.add(WidgetListItem.Header("Today"))
            result.addAll(grouped.today.map { WidgetListItem.ReminderItem(it) })
        }

        if (grouped.daily.isNotEmpty()) {
            result.add(WidgetListItem.Header("Daily"))
            result.addAll(grouped.daily.map { WidgetListItem.ReminderItem(it) })
        }

        if (grouped.weekly.isNotEmpty()) {
            result.add(WidgetListItem.Header("Weekly"))
            result.addAll(grouped.weekly.map { WidgetListItem.ReminderItem(it) })
        }

        if (grouped.monthly.isNotEmpty()) {
            result.add(WidgetListItem.Header("Monthly"))
            result.addAll(grouped.monthly.map { WidgetListItem.ReminderItem(it) })
        }

        grouped.upcoming.forEach { (date, reminders) ->
            result.add(WidgetListItem.Header(date))
            result.addAll(reminders.map { WidgetListItem.ReminderItem(it) })
        }

        return result
    }

    override fun onDestroy() {}

    override fun getCount(): Int = widgetItems.size

    override fun getViewAt(position: Int): RemoteViews {
        return try {
            val item = widgetItems.getOrNull(position) ?: return RemoteViews(context.packageName, R.layout.reminder_widget_item)

            when (item) {
                is WidgetListItem.Header -> {
                    val views = RemoteViews(context.packageName, R.layout.reminder_widget_header)
                    views.setTextViewText(R.id.header_title, item.title)
                    views
                }
                is WidgetListItem.ReminderItem -> {
                    val reminder = item.reminder
                    val views = RemoteViews(context.packageName, R.layout.reminder_widget_item)

                    views.setTextViewText(R.id.reminder_name, reminder.name)

                    val isRecurring = reminder.repeatInterval != RepeatInterval.NONE
                    val formatPattern = if (isRecurring) "HH:mm" else "MMM dd, HH:mm"
                    val timeStr = SimpleDateFormat(formatPattern, Locale.getDefault()).format(Date(reminder.deadline))

                    val remainingDaysInfo = if (isRecurring) {
                        val nowCal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val deadlineDate = Calendar.getInstance().apply {
                            timeInMillis = reminder.deadline
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }

                        val diffMillis = deadlineDate.timeInMillis - nowCal.timeInMillis
                        val diffDays = (diffMillis / (24 * 60 * 60 * 1000)).toInt()

                        when {
                            diffDays == 0 -> "Today"
                            diffDays == 1 -> "Tomorrow"
                            diffDays > 1 -> "In $diffDays days"
                            else -> null
                        }
                    } else null

                    val deadlineDisplayText = if (remainingDaysInfo != null) "$remainingDaysInfo, $timeStr" else timeStr
                    views.setTextViewText(R.id.reminder_deadline, deadlineDisplayText)

                    val isOverdue = reminder.deadline < System.currentTimeMillis()

                    val indicatorRes = if (isOverdue) {
                        R.drawable.widget_indicator_red
                    } else when (reminder.status) {
                        ReminderStatus.TODO -> R.drawable.widget_indicator_blue
                        ReminderStatus.IN_PROGRESS -> R.drawable.widget_indicator_yellow
                        ReminderStatus.DONE -> R.drawable.widget_indicator_green
                    }
                    views.setImageViewResource(R.id.status_indicator, indicatorRes)

                    views.setViewVisibility(R.id.repeat_icon, if (isRecurring) View.VISIBLE else View.GONE)

                    val fillInIntent = Intent().apply {
                        putExtra("reminderId", reminder.id)
                    }
                    views.setOnClickFillInIntent(R.id.reminder_item_layout, fillInIntent)
                    views
                }
            }
        } catch (e: Exception) {
            Log.e("Widget", "Error in getViewAt($position)", e)
            RemoteViews(context.packageName, R.layout.reminder_widget_item)
        }
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 2
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = false
}