package com.example.hw3.ui

import com.example.hw3.data.IShowRepository
import com.example.hw3.data.remote.ImageDto
import com.example.hw3.data.remote.RatingDto
import com.example.hw3.data.remote.ShowDto
import com.example.hw3.data.remote.toDomain
import com.example.hw3.model.Show
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException
import app.cash.turbine.test

private class FakeViewModelRepo(
    private val searchResult: (String) -> List<Show> = { emptyList() },
    private val searchDelay: Long = 0L,
    private val favourites: List<Show> = emptyList(),
    private val favouritesDelay: Long = 0L,
    private val throwOnSearch: String? = null,
    private val throwOnSearchAfterFirst: Boolean = false
) : IShowRepository {
    private var searchCallCount = 0

    override suspend fun searchShows(query: String): List<Show> {
        searchCallCount++
        if (searchDelay > 0) delay(searchDelay)
        if (throwOnSearch == query) {
            if (throwOnSearchAfterFirst && searchCallCount > 1) return searchResult(query)
            throw IOException("Network error")
        }
        return searchResult(query)
    }

    override suspend fun getShowById(id: Int): Show =
        searchResult("").firstOrNull { it.id == id } ?: error("not found")

    override suspend fun getFavourites(): List<Show> {
        if (favouritesDelay > 0) delay(favouritesDelay)
        return favourites
    }

    override suspend fun toggleFavourite(show: Show) = Unit
    override suspend fun isFavourite(id: Int): Boolean = false

    fun getSearchCallCount() = searchCallCount
}

@OptIn(ExperimentalCoroutinesApi::class)
class ShowViewModelTest {

    private lateinit var viewModel: ShowViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is EmptyQuery`() = runTest {
        viewModel = ShowViewModel(FakeViewModelRepo())
        assertTrue(viewModel.uiState.value is ShowListUiState.EmptyQuery)
    }

    @Test
    fun `searchShows emits Loading then Success`() = runTest {
        val shows = listOf(Show(1, "Test", "En", emptyList(), null, null, null))
        viewModel = ShowViewModel(FakeViewModelRepo(searchResult = { shows }))

        viewModel.uiState.test {
            assertTrue(awaitItem() is ShowListUiState.EmptyQuery)

            viewModel.onSearchQueryChange("Test")
            advanceTimeBy(500)
            advanceUntilIdle()

            assertTrue(awaitItem() is ShowListUiState.Loading)

            val success = awaitItem() as ShowListUiState.Success
            assertEquals(shows, success.shows)
            assertEquals("Test", success.searchQuery)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `searchShows emits Loading then Error`() = runTest {
        viewModel = ShowViewModel(FakeViewModelRepo(throwOnSearch = "Test"))

        viewModel.uiState.test {
            assertTrue(awaitItem() is ShowListUiState.EmptyQuery)

            viewModel.onSearchQueryChange("Test")
            advanceTimeBy(500)
            advanceUntilIdle()

            assertTrue(awaitItem() is ShowListUiState.Loading)   // обязательная промежуточная эмиссия

            val error = awaitItem() as ShowListUiState.Error
            assertEquals("Network error", error.message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry after error calls repository again via retryLastSearch`() = runTest {
        var callCount = 0
        val shows = listOf(Show(1, "Test", "En", emptyList(), null, null, null))
        val repo = object : IShowRepository {
            override suspend fun searchShows(query: String): List<Show> {
                callCount++
                return if (callCount == 1) throw IOException("Error") else shows
            }
            override suspend fun getShowById(id: Int): Show = error("unused")
            override suspend fun getFavourites(): List<Show> = emptyList()
            override suspend fun toggleFavourite(show: Show) = Unit
            override suspend fun isFavourite(id: Int): Boolean = false
        }

        viewModel = ShowViewModel(repo)
        viewModel.onSearchQueryChange("Test")
        advanceTimeBy(500)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ShowListUiState.Error)

        viewModel.retryLastSearch()
        advanceTimeBy(500)
        advanceUntilIdle()

        assertEquals(2, callCount)
        val state = viewModel.uiState.value
        assertTrue("Expected Success after retry, got $state", state is ShowListUiState.Success)
        assertEquals(1, (state as ShowListUiState.Success).shows.size)
    }

    @Test
    fun `empty search result emits NoResults`() = runTest {
        viewModel = ShowViewModel(FakeViewModelRepo(searchResult = { emptyList() }))
        viewModel.onSearchQueryChange("Empty")
        advanceTimeBy(500)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is ShowListUiState.NoResults)
    }

    @Test
    fun `ShowDto toDomain converts correctly`() {
        val dto = ShowDto(
            id = 1, name = "Breaking Bad", language = "English",
            genres = listOf("Drama", "Crime"), rating = RatingDto(9.5),
            image = ImageDto("url", null), summary = "<p>Summary</p>"
        )
        val show = dto.toDomain()
        assertEquals(1, show.id)
        assertEquals("Breaking Bad", show.name)
        assertEquals("English", show.language)
        assertEquals(listOf("Drama", "Crime"), show.genres)
        assertEquals(9.5, show.rating)
        assertEquals("url", show.imageUrl)
        assertEquals("Summary", show.summary)
    }

    @Test
    fun `favouritesUiState emits Loading then Success on first load`() = runTest {
        viewModel = ShowViewModel(FakeViewModelRepo(favourites = emptyList()))

        viewModel.favouritesUiState.test {
            viewModel.loadFavourites()
            advanceUntilIdle()

            assertTrue(awaitItem() is FavouritesUiState.Loading)
            val success = awaitItem() as FavouritesUiState.Success
            assertTrue(success.shows.isEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `outdated search result is ignored when newer query arrives`() = runTest {
        val repo = object : IShowRepository {
            override suspend fun searchShows(query: String): List<Show> {
                return when (query) {
                    "A" -> { delay(500); listOf(Show(1, "A", "", emptyList(), null, null, null)) }
                    "AB" -> { delay(50); listOf(Show(2, "AB", "", emptyList(), null, null, null)) }
                    else -> emptyList()
                }
            }
            override suspend fun getShowById(id: Int): Show = error("unused")
            override suspend fun getFavourites(): List<Show> = emptyList()
            override suspend fun toggleFavourite(show: Show) = Unit
            override suspend fun isFavourite(id: Int): Boolean = false
        }

        viewModel = ShowViewModel(repo)

        viewModel.uiState.test {
            assertTrue(awaitItem() is ShowListUiState.EmptyQuery)

            viewModel.onSearchQueryChange("A")
            advanceTimeBy(400)

            viewModel.onSearchQueryChange("AB")
            advanceTimeBy(600)
            advanceUntilIdle()

            assertTrue(awaitItem() is ShowListUiState.Loading)

            val success = awaitItem() as ShowListUiState.Success
            assertEquals(2, success.shows[0].id)
            assertEquals("AB", success.shows[0].name)

            cancelAndIgnoreRemainingEvents()
        }
    }
}