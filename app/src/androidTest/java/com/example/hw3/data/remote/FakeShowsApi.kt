package com.example.hw3.data.remote

import java.io.IOException

class FakeShowsApi : ShowsApi {
    private var shouldFail = true
    private val successResponse = listOf(
        SearchResponse(
            show = ShowDto(
                id = 1,
                name = "Success Show",
                language = "English",
                genres = listOf("Drama"),
                rating = RatingDto(8.5),
                image = ImageDto("https://...", null),
                summary = "<p>Great show</p>"
            )
        )
    )

    fun setShouldFail(fail: Boolean) {
        shouldFail = fail
    }

    override suspend fun searchShows(query: String): List<SearchResponse> {
        if (shouldFail) throw IOException("Network error")
        return successResponse
    }

    override suspend fun getShowById(id: Int): ShowDto {
        return successResponse.first().show
    }
}