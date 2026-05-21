package com.example.hw3

import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hw3.data.ShowRepository
import com.example.hw3.data.local.ShowDatabase
import com.example.hw3.data.remote.FakeShowsApi
import com.example.hw3.ui.ShowListUiState
import com.example.hw3.ui.ShowViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.test.*

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class RetryIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var database: ShowDatabase
    private lateinit var fakeApi: FakeShowsApi
    private lateinit var repository: ShowRepository
    private lateinit var viewModel: ShowViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ShowDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        fakeApi = FakeShowsApi()
        repository = ShowRepository(fakeApi, database.showDao())
        viewModel = ShowViewModel(repository)
    }

    @After
    fun teardown() {
        database.close()
        Dispatchers.resetMain()
    }

    @Test
    fun `retry after error works with fake api room and navgraph`() = runTest {
        composeTestRule.mainClock.autoAdvance = true
        fakeApi.setShouldFail(true)

        composeTestRule.setContent {
            NavGraph(startDestination = "list", viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Название сериала").performTextInput("Query")
        advanceTimeBy(500)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ShowListUiState.Error)

        fakeApi.setShouldFail(false)

        composeTestRule.onNodeWithText("Повторить").performClick()
        advanceTimeBy(500)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ShowListUiState.Success)
        assertEquals("Success Show", (state as ShowListUiState.Success).shows[0].name)
    }
}