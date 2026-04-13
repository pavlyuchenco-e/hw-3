package com.example.hw3.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hw3.data.ShowRepository
import com.example.hw3.model.Show
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class ShowListUiState(
    val searchQuery: String = "",
    val showList: List<Show> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false,
)

class ShowViewModel(
    private val repository: ShowRepository = ShowRepository()
) : ViewModel() {

    var uiState by mutableStateOf(ShowListUiState())
        private set

    private var searchJob: Job? = null

    fun onSearchQueryChange(newValue: String) {
        uiState = uiState.copy(
            searchQuery = newValue,
            errorMessage = null
        )

        searchJob?.cancel()

        val query = newValue.trim()

        if (query.isBlank()) {
            uiState = uiState.copy(
                showList = emptyList(),
                isLoading = false,
                errorMessage = null,
                hasSearched = false
            )
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)

            uiState = uiState.copy(
                isLoading = true,
                errorMessage = null
            )

            try {
                val result = repository.searchShows(query)

                if (query != uiState.searchQuery.trim()) return@launch

                uiState = uiState.copy(
                    showList = result,
                    isLoading = false,
                    errorMessage = null,
                    hasSearched = true
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = "Ошибка загрузки: ${e.message}",
                    hasSearched = true
                )
            }
        }
    }
    fun getShowById(id: Int): Show? {
        return uiState.showList.find { it.id == id }
    }
}