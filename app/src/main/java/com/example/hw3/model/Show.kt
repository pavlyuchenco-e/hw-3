package com.example.hw3.model

data class Show(
    val id: Int,
    val name: String,
    val language: String,
    val genres: List<String>,
    val rating: Double?,
    val imageUrl: String?,
    val summary: String?
)