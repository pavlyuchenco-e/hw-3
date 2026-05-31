package com.example.hw3.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromGenresList(genres: List<String>): String {
        return Gson().toJson(genres)
    }

    @TypeConverter
    fun toGenresList(genresString: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(genresString, type)
    }
}