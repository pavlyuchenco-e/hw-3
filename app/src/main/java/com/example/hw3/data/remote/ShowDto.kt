package com.example.hw3.data.remote

import com.example.hw3.model.Show
import com.google.gson.annotations.SerializedName

data class SearchResponse(
    val show: ShowDto
)

data class ShowDto(
    val id: Int,
    val name: String? = null,
    val language: String? = null,
    val genres: List<String>? = null,
    val rating: RatingDto? = null,
    val image: ImageDto? = null,
    val summary: String? = null
)

data class RatingDto(
    val average: Double? = null
)

data class ImageDto(
    val medium: String? = null,
    val original: String? = null
)

fun ShowDto.toDomain(): Show {
    return Show(
        id = id,
        name = name ?: "Unknown",
        language = language ?: "Unknown",
        genres = genres ?: emptyList(),
        rating = rating?.average,
        imageUrl = image?.medium ?: image?.original,
        summary = summary?.replace(Regex("<[^>]*>"), "")
    )
}