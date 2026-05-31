package com.example.hw3

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hw3.data.ShowRepository
import com.example.hw3.data.local.ShowDatabase
import com.example.hw3.data.remote.FakeShowsApi
import com.example.hw3.ui.ShowListUiState
import com.example.hw3.ui.ShowViewModel
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class RetryIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var database: ShowDatabase
    private lateinit var fakeApi: FakeShowsApi
    private lateinit var repository: ShowRepository
    private lateinit var viewModel: ShowViewModel

    @Before
    fun setup() {
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
    }

    @Test
    fun `retry after error works with fake api room and navgraph`() {
        fakeApi.setShouldFail(true)

        composeTestRule.setContent {
            NavGraph(startDestination = "list", viewModel = viewModel)
        }

        composeTestRule
            .onNode(hasSetTextAction())
            .performTextInput("Query")

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.uiState.value is ShowListUiState.Error
        }
        assertTrue(viewModel.uiState.value is ShowListUiState.Error)

        fakeApi.setShouldFail(false)

        composeTestRule
            .onNodeWithText("Повторить")
            .performClick()

        // 6. Ждём Success
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            viewModel.uiState.value is ShowListUiState.Success
        }

        val state = viewModel.uiState.value as ShowListUiState.Success
        assertEquals("Success Show", state.shows[0].name)
    }
}