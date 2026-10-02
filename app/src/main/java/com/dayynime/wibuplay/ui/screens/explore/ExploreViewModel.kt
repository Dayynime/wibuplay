package com.dayynime.wibuplay.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayynime.wibuplay.data.model.AnimeItem
import com.dayynime.wibuplay.data.model.GenreItem
import com.dayynime.wibuplay.data.repository.AnimeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExploreUiState(
    val query: String = "",
    val selectedGenreId: String? = null,
    val selectedType: String? = null,
    val selectedYear: String? = null,
    val selectedStudio: String? = null,
    val selectedSort: String = "views", // "views" or "alphabet"
    val genres: List<GenreItem> = emptyList(),
    val items: List<AnimeItem> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isEndOfList: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 0
)

class ExploreViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExploreUiState())
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadGenres()
        loadAnime(page = 0, isInitial = true)
    }

    private fun loadGenres() {
        viewModelScope.launch {
            repository.getGenres().onSuccess { genres ->
                _uiState.update { it.copy(genres = genres) }
            }
        }
    }

    fun onQueryChange(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400) // debounce
            loadAnime(page = 0, isInitial = true)
        }
    }

    fun onGenreSelected(genreId: String?) {
        val newGenre = if (_uiState.value.selectedGenreId == genreId) null else genreId
        _uiState.update { it.copy(selectedGenreId = newGenre, selectedType = null, selectedYear = null) }
        loadAnime(page = 0, isInitial = true)
    }

    fun onTypeSelected(type: String?) {
        val newType = if (_uiState.value.selectedType == type) null else type
        _uiState.update { it.copy(selectedType = newType, selectedGenreId = null, selectedYear = null) }
        loadAnime(page = 0, isInitial = true)
    }

    fun onSortSelected(sort: String) {
        if (_uiState.value.selectedSort == sort) return
        _uiState.update { it.copy(selectedSort = sort) }
        loadAnime(page = 0, isInitial = true)
    }

    fun onYearSelected(year: String?) {
        val newYear = if (_uiState.value.selectedYear == year) null else year
        _uiState.update { it.copy(selectedYear = newYear, selectedGenreId = null, selectedType = null) }
        loadAnime(page = 0, isInitial = true)
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                query = "",
                selectedGenreId = null,
                selectedType = null,
                selectedYear = null,
                selectedStudio = null,
                selectedSort = "views"
            )
        }
        loadAnime(page = 0, isInitial = true)
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || state.isEndOfList) return
        loadAnime(page = state.currentPage + 1, isInitial = false)
    }

    private fun loadAnime(page: Int, isInitial: Boolean) {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update {
                it.copy(
                    isLoading = isInitial,
                    isLoadingMore = !isInitial,
                    error = null
                )
            }

            val result = repository.explore(
                keyword = state.query,
                genreId = state.selectedGenreId,
                type = state.selectedType,
                year = state.selectedYear,
                studio = state.selectedStudio,
                sort = state.selectedSort,
                page = page
            )

            result.fold(
                onSuccess = { newItems ->
                    _uiState.update { current ->
                        val combined = if (isInitial) newItems else current.items + newItems
                        current.copy(
                            items = combined,
                            currentPage = page,
                            isEndOfList = newItems.isEmpty(),
                            isLoading = false,
                            isLoadingMore = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = if (isInitial) error.localizedMessage ?: "Gagal memuat anime" else null
                        )
                    }
                }
            )
        }
    }
}
