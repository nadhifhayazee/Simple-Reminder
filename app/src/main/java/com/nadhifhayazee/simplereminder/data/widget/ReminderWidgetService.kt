package com.nadhifhayazee.simplereminder.data.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.nadhifhayazee.simplereminder.R
import com.nadhifhayazee.simplereminder.domain.model.Reminder
import com.nadhifhayazee.simplereminder.domain.model.ReminderStatus
import com.nadhifhayazee.simplereminder.domain.repository.ReminderRepository
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

class ReminderRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WidgetEntryPoint {
        fun repository(): ReminderRepository
    }

    private lateinit var repository: ReminderRepository
    private var reminders: List<Reminder> = emptyList()
    private val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())

    override fun onCreate() {
        Log.d("Widget", "RemoteViewsFactory onCreate")
        try {
            val entryPoint = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
            repository = entryPoint.repository()
        } catch (e: Exception) {
            Log.e("Widget", "Failed to get repository", e)
        }
    }

    override fun onDataSetChanged() {
        Log.d("Widget", "onDataSetChanged started")
        try {
            runBlocking {
                withTimeoutOrNull(3000L) {
                    reminders = repository.getReminders().first()
                    Log.d("Widget", "Fetched ${reminders.size} reminders")
                } ?: Log.e("Widget", "Timed out waiting for reminders")
            }
        } catch (e: Exception) {
            Log.e("Widget", "Error fetching reminders", e)
        }
    }

    override fun onDestroy() {}

    override fun getCount(): Int = reminders.size

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.reminder_widget_item)
        val reminder = reminders.getOrNull(position) ?: return views

        views.setTextViewText(R.id.reminder_name, reminder.name)
        views.setTextViewText(R.id.reminder_deadline, "Due ${dateFormat.format(Date(reminder.deadline))}")

        val indicatorRes = when (reminder.status) {
            ReminderStatus.TODO -> R.drawable.widget_indicator_blue
            ReminderStatus.IN_PROGRESS -> R.drawable.widget_indicator_yellow
            ReminderStatus.DONE -> R.drawable.widget_indicator_green
        }
        
        // Use setImageViewResource as it's more reliable for RemoteViews
        views.setImageViewResource(R.id.status_indicator, indicatorRes)
        
        // Fill-in intent to pass reminderId back to MainActivity
        val fillInIntent = Intent().apply {
            putExtra("reminderId", reminder.id)
        }
        views.setOnClickFillInIntent(R.id.reminder_item_layout, fillInIntent)
        
        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = 
        reminders.getOrNull(position)?.id?.toLong() ?: position.toLong()
    override fun hasStableIds(): Boolean = true
}
