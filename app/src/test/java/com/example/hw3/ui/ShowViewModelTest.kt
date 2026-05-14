package com.example.hw3.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import app.cash.turbine.test
import com.example.hw3.data.ShowRepository
import com.example.hw3.data.remote.ImageDto
import com.example.hw3.data.remote.RatingDto
import com.example.hw3.data.remote.ShowDto
import com.example.hw3.data.remote.toDomain
import com.example.hw3.model.Show
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ShowViewModelTest {

    private lateinit var viewModel: ShowViewModel
    private val repository: ShowRepository = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

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
        viewModel = ShowViewModel(repository)
        assertTrue(viewModel.uiState is ShowListUiState.EmptyQuery)
    }

    @Test
    fun `searchShows emits Loading then Success`() = runTest {
        val shows = listOf(Show(1, "Test", "En", emptyList(), null, null, null))
        coEvery { repository.searchShows("Test") } returns shows

        viewModel = ShowViewModel(repository)
        viewModel.onSearchQueryChange("Test")
        advanceUntilIdle()

        val state = viewModel.uiState
        when (state) {
            is ShowListUiState.Success -> {
                assertEquals(shows, state.shows)
                assertEquals("Test", state.searchQuery)
            }
            else -> fail("Expected Success state")
        }
    }

    @Test
    fun `searchShows emits Loading then Error on exception`() = runTest {
        coEvery { repository.searchShows("Test") } throws IOException("Network error")

        viewModel = ShowViewModel(repository)
        viewModel.onSearchQueryChange("Test")
        advanceUntilIdle()

        val state = viewModel.uiState
        when (state) {
            is ShowListUiState.Error -> assertEquals("Network error", state.message)
            else -> fail("Expected Error state")
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
        advanceUntilIdle()
        assertTrue(viewModel.uiState is ShowListUiState.Error)

        viewModel.onSearchQueryChange("Test")
        advanceUntilIdle()
        when (val state = viewModel.uiState) {
            is ShowListUiState.Success -> assertEquals(1, state.shows.size)
            else -> fail("Expected Success after retry")
        }
    }

    @Test
    fun `empty search result emits NoResults`() = runTest {
        coEvery { repository.searchShows("Empty") } returns emptyList()
        viewModel = ShowViewModel(repository)
        viewModel.onSearchQueryChange("Empty")
        advanceUntilIdle()
        assertTrue(viewModel.uiState is ShowListUiState.NoResults)
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
            // Первая эмиссия: Loading
            val loading = awaitItem()
            assertTrue(loading is FavouritesUiState.Loading)
            // Вторая эмиссия: Success
            val success = awaitItem() as FavouritesUiState.Success
            assertTrue(success.shows.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `outdated search result is ignored`() = runTest {
        coEvery { repository.searchShows("A") } returns listOf(Show(1, "A", "", emptyList(), null, null, null))
        coEvery { repository.searchShows("AB") } returns listOf(Show(2, "AB", "", emptyList(), null, null, null))

        viewModel = ShowViewModel(repository)

        viewModel.onSearchQueryChange("A")
        viewModel.onSearchQueryChange("AB")
        advanceTimeBy(500)
        runCurrent()

        val state = viewModel.uiState
        assertTrue(state is ShowListUiState.Success)
        assertEquals(2, (state as ShowListUiState.Success).shows[0].id)
    }
}