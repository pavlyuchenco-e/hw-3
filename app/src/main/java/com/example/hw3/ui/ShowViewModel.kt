package com.example.hw3.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hw3.data.ShowRepository
import androidx.compose.runtime.State
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

sealed class ShowDetailUiState {
    object Loading : ShowDetailUiState()
    data class Success(val show: Show) : ShowDetailUiState()
    data class Error(val message: String) : ShowDetailUiState()
}

class ShowViewModel(
    private val repository: ShowRepository = ShowRepository()
) : ViewModel() {
    var searchQuery by mutableStateOf("")
        private set

    var uiState by mutableStateOf<ShowListUiState>(ShowListUiState.EmptyQuery)
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

    private val _detailUiState = mutableStateOf<ShowDetailUiState>(ShowDetailUiState.Loading)
    val detailUiState: State<ShowDetailUiState> = _detailUiState

    private var detailJob: Job? = null
    private var expectedShowId: Int? = null

    fun resetDetailState() {
        _detailUiState.value = ShowDetailUiState.Loading
        expectedShowId = null
        detailJob?.cancel()
    }

    fun loadShowById(showId: Int) {
        expectedShowId = showId
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            _detailUiState.value = ShowDetailUiState.Loading
            try {
                val show = repository.getShowById(showId)
                if (expectedShowId != showId) return@launch
                _detailUiState.value = ShowDetailUiState.Success(show = show)
            } catch (e: CancellationException) {
                // ignore
            } catch (e: Exception) {
                if (expectedShowId != showId) return@launch
                _detailUiState.value = ShowDetailUiState.Error(message = e.message ?: "Ошибка загрузки")
            }
        }
    }
}