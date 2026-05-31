package com.example.hw3.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hw3.data.IShowRepository
import com.example.hw3.model.Show
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException
import javax.inject.Inject

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

sealed class FavouritesUiState {
    object Loading : FavouritesUiState()
    data class Success(val shows: List<Show>) : FavouritesUiState()
    data class Error(val message: String) : FavouritesUiState()
}

@HiltViewModel
class ShowViewModel @Inject constructor(
    private val repository: IShowRepository
) : ViewModel() {

    var searchQuery by mutableStateOf("")
        private set

    private val _uiState = MutableStateFlow<ShowListUiState>(ShowListUiState.EmptyQuery)
    val uiState: StateFlow<ShowListUiState> = _uiState.asStateFlow()

    private val _detailUiState = mutableStateOf<ShowDetailUiState>(ShowDetailUiState.Loading)
    val detailUiState: State<ShowDetailUiState> = _detailUiState

    private val _favouritesUiState = MutableStateFlow<FavouritesUiState>(FavouritesUiState.Loading)
    val favouritesUiState: StateFlow<FavouritesUiState> = _favouritesUiState.asStateFlow()

    private var searchJob: Job? = null
    private var detailJob: Job? = null
    private var expectedShowId: Int? = null
    private var favouritesJob: Job? = null
    private var lastSearchQuery: String = ""
    private var favouritesRequestId = 0

    fun loadFavourites() {
        val requestId = ++favouritesRequestId
        favouritesJob?.cancel()
        favouritesJob = viewModelScope.launch {
            _favouritesUiState.value = FavouritesUiState.Loading
            try {
                val favourites = repository.getFavourites()
                if (requestId != favouritesRequestId) return@launch
                _favouritesUiState.value = FavouritesUiState.Success(favourites)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (requestId != favouritesRequestId) return@launch
                _favouritesUiState.value =
                    FavouritesUiState.Error(e.message ?: "Ошибка загрузки избранного")
            }
        }
    }

    fun toggleFavourite(show: Show) {
        viewModelScope.launch {
            try {
                repository.toggleFavourite(show)
                if (_detailUiState.value is ShowDetailUiState.Success) {
                    val current = (_detailUiState.value as ShowDetailUiState.Success).show
                    if (current.id == show.id) {
                        _detailUiState.value = ShowDetailUiState.Success(
                            current.copy(isFavourite = !current.isFavourite)
                        )
                    }
                }
                if (_uiState.value is ShowListUiState.Success) {
                    val currentState = _uiState.value as ShowListUiState.Success
                    val updatedShows = currentState.shows.map {
                        if (it.id == show.id) it.copy(isFavourite = !it.isFavourite) else it
                    }
                    _uiState.value = ShowListUiState.Success(updatedShows, currentState.searchQuery)
                }
                loadFavourites()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {

            }
        }
    }

    fun retryLastSearch() {
        if (lastSearchQuery.isNotBlank()) {
            onSearchQueryChange(lastSearchQuery)
        }
    }

    fun onSearchQueryChange(newValue: String) {
        searchQuery = newValue
        val query = newValue.trim()
        lastSearchQuery = query

        if (query.isBlank()) {
            _uiState.value = ShowListUiState.EmptyQuery
            searchJob?.cancel()
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            _uiState.value = ShowListUiState.Loading
            try {
                val result = repository.searchShows(query)
                if (query != searchQuery.trim()) return@launch
                _uiState.value = if (result.isEmpty()) {
                    ShowListUiState.NoResults
                } else {
                    ShowListUiState.Success(shows = result, searchQuery = query)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (query != searchQuery.trim()) return@launch
                _uiState.value = ShowListUiState.Error(
                    message = e.message ?: "Ошибка загрузки",
                    searchQuery = query
                )
            }
        }
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

            } catch (e: Exception) {
                if (expectedShowId != showId) return@launch
                _detailUiState.value =
                    ShowDetailUiState.Error(message = e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun resetDetailState() {
        _detailUiState.value = ShowDetailUiState.Loading
        expectedShowId = null
        detailJob?.cancel()
    }
}