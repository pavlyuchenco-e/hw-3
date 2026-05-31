package com.example.hw3.data

import com.example.hw3.model.Show

interface IShowRepository {
    suspend fun searchShows(query: String): List<Show>
    suspend fun getShowById(id: Int): Show
    suspend fun getFavourites(): List<Show>
    suspend fun toggleFavourite(show: Show)
    suspend fun isFavourite(id: Int): Boolean
}