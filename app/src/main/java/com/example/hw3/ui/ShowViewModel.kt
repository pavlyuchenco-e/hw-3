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

sealed class ShowListUiState {
    object Loading : ShowListUiState()
    data class Success(val shows: List<Show>, val searchQuery: String) : ShowListUiState()
    data class Error(val message: String, val searchQuery: String) : ShowListUiState()
    object EmptyQuery : ShowListUiState()
    object NoResults : ShowListUiState()
}

data class ShowDetailUiState(
    val isLoading: Boolean = false,
    val show: Show? = null,
    val error: String? = null
)

class ShowViewModel(
    private val repository: ShowRepository = ShowRepository()
) : ViewModel() {
    var searchQuery by mutableStateOf("")
        private set

    var uiState by mutableStateOf<ShowListUiState>(ShowListUiState.EmptyQuery)
        private set

    var detailUiState by mutableStateOf(ShowDetailUiState())
        private set

    private var searchJob: Job? = null

    fun onSearchQueryChange(newValue: String) {
        searchQuery = newValue
        val query = newValue.trim()

        if (query.isBlank()) {
            uiState = ShowListUiState.EmptyQuery
            searchJob?.cancel()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)

            uiState = ShowListUiState.Loading

            try {
                val result = repository.searchShows(query)

                if (query != searchQuery.trim()) return@launch

                uiState = if (result.isEmpty()) {
                    ShowListUiState.NoResults
                } else {
                    ShowListUiState.Success(shows = result, searchQuery = query)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (query != searchQuery.trim()) return@launch
                uiState = ShowListUiState.Error(
                    message = e.message ?: "Ошибка загрузки",
                    searchQuery = query
                )
            }
        }
    }

    fun loadShowById(showId: Int) {
        viewModelScope.launch {
            detailUiState = ShowDetailUiState(isLoading = true)
            try {
                val show = repository.getShowById(showId)
                detailUiState = ShowDetailUiState(show = show)
            } catch (e: Exception) {
                detailUiState = ShowDetailUiState(
                    error = e.message ?: "Ошибка загрузки"
                )
            }
        }
    }
}