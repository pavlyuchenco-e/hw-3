package com.example.hw3.data.remote

import com.example.hw3.model.Show
import com.google.gson.annotations.SerializedName

data class SearchResponse(
    val show: ShowDto
)

data class ShowDto(
    val id: Int,
    val name: String,
    val language: String?,
    val genres: List<String>?,
    val rating: RatingDto?,
    val image: ImageDto?,
    val summary: String?
)

data class RatingDto(
    val average: Double?
)

data class ImageDto(
    val medium: String?,
    val original: String?
)

fun ShowDto.toDomain(): Show {
    return Show(
        id = id,
        name = name,
        language = language ?: "Unknown",
        genres = genres ?: emptyList(),
        rating = rating?.average,
        imageUrl = image?.medium ?: image?.original,
        summary = summary?.replace(Regex("<[^>]*>"), "")
    )
}