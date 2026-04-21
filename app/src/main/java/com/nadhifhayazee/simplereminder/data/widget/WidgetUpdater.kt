package com.nadhifhayazee.simplereminder.data.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun updateWidget() {
        val intent = Intent(context, ReminderWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        }
        context.sendBroadcast(intent)
    }
}
