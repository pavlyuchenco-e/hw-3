package com.example.hw3

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hw3.data.IShowRepository
import com.example.hw3.model.Show
import com.example.hw3.ui.ShowViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private class FakeNavRepo(
    private val searchResult: List<Show> = emptyList(),
    private val showByIdMap: Map<Int, Show> = emptyMap()
) : IShowRepository {
    override suspend fun searchShows(query: String): List<Show> = searchResult
    override suspend fun getShowById(id: Int): Show =
        showByIdMap[id] ?: error("showById не задан для id=$id")
    override suspend fun getFavourites(): List<Show> = emptyList()
    override suspend fun toggleFavourite(show: Show) = Unit
    override suspend fun isFavourite(id: Int): Boolean = false
}

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testShow = Show(1, "Breaking", "English", emptyList(), null, null, null, false)

    @Test
    fun `after successful search show card is displayed`() {
        val viewModel = ShowViewModel(FakeNavRepo(searchResult = listOf(testShow)))

        composeTestRule.setContent {
            NavGraph(startDestination = "list", viewModel = viewModel)
        }

        composeTestRule
            .onNode(hasSetTextAction())
            .performTextInput("Breaking")

        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule
                .onAllNodesWithText(testShow.name)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }

        composeTestRule
            .onAllNodesWithText(testShow.name)[0]
            .assertIsDisplayed()
    }
}