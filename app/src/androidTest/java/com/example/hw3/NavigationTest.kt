package com.example.hw3

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hw3.data.ShowRepository
import com.example.hw3.model.Show
import com.example.hw3.ui.ShowViewModel
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
        val testShow = Show(1, "Breaking", "English", emptyList(), null, null, null, false)
        coEvery { mockRepo.searchShows("Breaking") } returns listOf(testShow)

        val viewModel = ShowViewModel(mockRepo)
        composeTestRule.setContent {
            NavGraph(startDestination = "list", viewModel = viewModel)
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
        val mockRepo = mockk<ShowRepository>(relaxed = true)
        val testShow = Show(1, "Breaking", "English", emptyList(), null, null, null, false)
        coEvery { mockRepo.searchShows("Breaking") } returns listOf(testShow)
        val viewModel = ShowViewModel(mockRepo)

        composeTestRule.setContent {
            NavGraph(startDestination = "list", viewModel = viewModel)
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