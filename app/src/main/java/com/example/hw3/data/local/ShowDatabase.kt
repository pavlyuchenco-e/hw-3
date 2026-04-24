package com.example.hw3.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [FavouriteShowEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ShowDatabase : RoomDatabase() {
    abstract fun showDao(): ShowDao
}