package com.example.hw3.di

import android.content.Context
import androidx.room.Room
import com.example.hw3.data.local.ShowDao
import com.example.hw3.data.local.ShowDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideShowDatabase(
        @ApplicationContext context: Context
    ): ShowDatabase =
        Room.databaseBuilder(
            context,
            ShowDatabase::class.java,
            "show.db",
        ).build()

    @Provides
    @Singleton
    fun provideShowDao(
        database: ShowDatabase
    ): ShowDao = database.showDao()

}