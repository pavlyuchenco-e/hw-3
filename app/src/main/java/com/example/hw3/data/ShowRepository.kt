package com.example.hw3.data

import com.example.hw3.NetworkModule
import com.example.hw3.data.remote.ShowsApi
import com.example.hw3.data.remote.toDomain
import com.example.hw3.model.Show
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShowRepository(private val api: ShowsApi = NetworkModule.api) {
    suspend fun searchShows(query: String): List<Show> = withContext(Dispatchers.IO) {
        val response = api.searchShows(query)
        response.mapNotNull { it.show.toDomain() }
    }
}