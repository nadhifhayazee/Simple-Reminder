package com.nadhifhayazee.simplereminder.data.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import com.nadhifhayazee.simplereminder.domain.widget.WidgetUpdater
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WidgetUpdaterImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WidgetUpdater {
    override fun updateWidget() {
        val intent = Intent(context, ReminderWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        context.sendBroadcast(intent)
    }
}
