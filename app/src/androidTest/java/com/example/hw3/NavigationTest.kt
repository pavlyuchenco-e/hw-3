package com.example.hw3

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hw3.data.ShowRepository
import com.example.hw3.model.Show
import com.example.hw3.ui.ShowViewModel
import com.example.hw3.ui.screens.ShowDetailScreen
import com.example.hw3.ui.screens.ShowListScreen
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `after successful search shows list is displayed`() {
        val mockRepo = mockk<ShowRepository>()
        val testShow = Show(
            id = 1,
            name = "Breaking",
            language = "English",
            genres = emptyList(),
            rating = null,
            imageUrl = null,
            summary = null,
            isFavourite = false
        )
        coEvery { mockRepo.searchShows("Breaking") } returns listOf(testShow)

        val viewModel = ShowViewModel(mockRepo)
        composeTestRule.setContent {
            TestNavGraph(viewModel)
        }
        composeTestRule.onNodeWithText("Название сериала").performTextInput("Breaking")
        composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Breaking").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Breaking").assertIsDisplayed()
    }

    @Test
    fun `click on show card opens detail screen`() {
        val mockRepo = mockk<ShowRepository>()
        val testShow = Show(
            id = 1,
            name = "Breaking",
            language = "English",
            genres = emptyList(),
            rating = null,
            imageUrl = null,
            summary = null,
            isFavourite = false
        )
        coEvery { mockRepo.searchShows("Breaking") } returns listOf(testShow)

        val viewModel = ShowViewModel(mockRepo)

        composeTestRule.setContent {
            TestNavGraph(viewModel)
        }

        composeTestRule.onNodeWithText("Название сериала").performTextInput("Breaking")
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Breaking").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText("Breaking").performClick()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
    }
}

@Composable
fun TestNavGraph(viewModel: ShowViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            ShowListScreen(
                searchQuery = viewModel.searchQuery,
                uiState = viewModel.uiState,
                onSearchChange = viewModel::onSearchQueryChange,
                onShowClick = { show -> navController.navigate("detail/${show.id}") },
                onNavigateToFavourites = {}
            )
        }
        composable("detail/{showId}") { backStackEntry ->
            val showId = backStackEntry.arguments?.getInt("showId") ?: return@composable
            val detailState by viewModel.detailUiState
            ShowDetailScreen(
                uiState = detailState,
                onBackPressed = { navController.popBackStack() },
                onRetry = { viewModel.loadShowById(showId) },
                onToggleFavourite = {}
            )
        }
    }
}