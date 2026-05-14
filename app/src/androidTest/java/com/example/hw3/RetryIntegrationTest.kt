package com.example.hw3

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hw3.data.ShowRepository
import com.example.hw3.model.Show
import com.example.hw3.ui.ShowViewModel
import com.example.hw3.ui.screens.ShowListScreen
import io.mockk.coEvery
import kotlinx.coroutines.test.advanceUntilIdle
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RetryIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `error then retry shows success using mockk`() = runTest {
        val mockRepository = mockk<ShowRepository>(relaxed = true)
        val viewModel = ShowViewModel(mockRepository)

        val successShows = listOf(Show(1, "Success Show", "English", emptyList(), 8.5, null, null))
        coEvery { mockRepository.searchShows("Query") } throws IOException("Network error") andThen successShows

        composeTestRule.setContent {
            ShowListScreen(
                searchQuery = viewModel.searchQuery,
                uiState = viewModel.uiState,
                onSearchChange = viewModel::onSearchQueryChange,
                onShowClick = {},
                onNavigateToFavourites = {}
            )
        }

        composeTestRule.onNodeWithText("Название сериала").performTextInput("Query")
        composeTestRule.waitForIdle()
        advanceUntilIdle()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Повторить").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Повторить").performClick()
        composeTestRule.waitForIdle()
        advanceUntilIdle()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Success Show").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Success Show").assertIsDisplayed()
    }
}