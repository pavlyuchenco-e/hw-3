package com.example.hw3.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hw3.model.Show
import org.intellij.lang.annotations.Language

@Entity(tableName = "favourite_show")
data class FavouriteShowEntity (
    @PrimaryKey
    val id: Int,
    val name: String,
    val language: String,
    val genres: List<String>,
    val rating: Double?,
    val imageUrl: String?,
    val summary: String?
)

fun FavouriteShowEntity.toDomain(): Show = Show(
    id,
    name,
    language,
    genres,
    rating,
    imageUrl,
    summary,
    isFavourite = true,
)

fun Show.toFavouriteEntity(): FavouriteShowEntity = FavouriteShowEntity(
    id,
    name,
    language,
    genres,
    rating,
    imageUrl,
    summary,
)