package com.nadhifhayazee.simplereminder.di

import com.nadhifhayazee.simplereminder.data.notification.ReminderNotificationScheduler
import com.nadhifhayazee.simplereminder.domain.notification.NotificationScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    @Binds
    @Singleton
    abstract fun bindNotificationScheduler(
        reminderNotificationScheduler: ReminderNotificationScheduler
    ): NotificationScheduler
}
