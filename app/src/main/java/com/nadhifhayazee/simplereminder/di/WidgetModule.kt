package com.nadhifhayazee.simplereminder.di

import com.nadhifhayazee.simplereminder.data.widget.WidgetUpdaterImpl
import com.nadhifhayazee.simplereminder.domain.widget.WidgetUpdater
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WidgetModule {
    @Binds
    @Singleton
    abstract fun bindWidgetUpdater(
        widgetUpdaterImpl: WidgetUpdaterImpl
    ): WidgetUpdater
}