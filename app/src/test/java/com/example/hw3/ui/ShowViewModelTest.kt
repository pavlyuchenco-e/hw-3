package com.example.hw3.ui

import com.example.hw3.data.ShowRepository
import com.example.hw3.data.remote.ImageDto
import com.example.hw3.data.remote.RatingDto
import com.example.hw3.data.remote.ShowDto
import com.example.hw3.data.remote.toDomain
import com.example.hw3.model.Show
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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

@OptIn(ExperimentalCoroutinesApi::class)
class ShowViewModelTest {

    private lateinit var viewModel: ShowViewModel
    private val repository: ShowRepository = mockk(relaxed = true)
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
    fun `searchShows emits Loading then Success`() = runTest {
        val shows = listOf(Show(1, "Test", "En", emptyList(), null, null, null))
        coEvery { repository.searchShows("Test") } returns shows

        viewModel = ShowViewModel(repository)

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
        coEvery { repository.searchShows("Test") } throws IOException("Network error")

        viewModel = ShowViewModel(repository)

        viewModel.uiState.test {
            assertTrue(awaitItem() is ShowListUiState.EmptyQuery)
            viewModel.onSearchQueryChange("Test")
            advanceTimeBy(500)
            advanceUntilIdle()
            assertTrue(awaitItem() is ShowListUiState.Loading)

            val error = awaitItem() as ShowListUiState.Error
            assertEquals("Network error", error.message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `retry after error calls repository again`() = runTest {
        var callCount = 0
        coEvery { repository.searchShows("Test") } answers {
            callCount++
            if (callCount == 1) throw IOException("Error")
            else listOf(Show(1, "Test", "En", emptyList(), null, null, null))
        }

        viewModel = ShowViewModel(repository)
        viewModel.onSearchQueryChange("Test")
        advanceTimeBy(500)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is ShowListUiState.Error)

        viewModel.retryLastSearch()
        advanceTimeBy(500)
        advanceUntilIdle()

        coVerify(exactly = 2) { repository.searchShows("Test") }
        when (val state = viewModel.uiState.value) {
            is ShowListUiState.Success -> assertEquals(1, state.shows.size)
            else -> fail("Expected Success after retry")
        }
    }

    @Test
    fun `empty search result emits NoResults`() = runTest {
        coEvery { repository.searchShows("Empty") } returns emptyList()
        viewModel = ShowViewModel(repository)
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
        coEvery { repository.getFavourites() } returns emptyList()
        viewModel = ShowViewModel(repository)

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
    fun `consecutive loadFavourites cancels previous and emits fresh data`() = runTest {
        val oldShows = listOf(Show(1, "Old", "En", emptyList(), null, null, null, false))
        val newShows = listOf(Show(2, "New", "En", emptyList(), null, null, null, false))

        var firstCall = true

        coEvery { repository.getFavourites() } coAnswers {
            if (firstCall){
                firstCall = false
                delay(1000)
                oldShows
            } else{
                newShows
            }
        }
        viewModel = ShowViewModel(repository)

        viewModel.loadFavourites()
        advanceTimeBy(100)
        viewModel.loadFavourites()
        advanceUntilIdle()

        val state = viewModel.favouritesUiState.value
        assertTrue(state is FavouritesUiState.Success)
        state as FavouritesUiState.Success
        assertEquals("New", state.shows.first().name)
    }

    @Test
    fun `outdated search result is ignored`() = runTest {
        coEvery { repository.searchShows("A") } coAnswers {
            delay(200)
            listOf(Show(1, "A", "", emptyList(), null, null, null))
        }
        coEvery { repository.searchShows("AB") } coAnswers {
            delay(50)
            listOf(Show(2, "AB", "", emptyList(), null, null, null))
        }

        viewModel = ShowViewModel(repository)

        viewModel.onSearchQueryChange("A")
        viewModel.onSearchQueryChange("AB")
        advanceTimeBy(500)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ShowListUiState.Success)
        assertEquals(2, (state as ShowListUiState.Success).shows[0].id)
    }
}