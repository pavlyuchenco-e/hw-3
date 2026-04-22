package com.example.hw3.data.remote

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ShowsApi {
    @GET("search/shows")
    suspend fun searchShows(
        @Query("q") query: String
    ): List<SearchResponse>

    @GET("shows/{id}")
    suspend fun getShowById(@Path("id") id: Int): ShowDto
}