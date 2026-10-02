package com.dayynime.wibuplay.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayynime.wibuplay.data.model.AnimeItem
import com.dayynime.wibuplay.data.model.CuplixItem
import com.dayynime.wibuplay.data.model.EpisodeItem
import com.dayynime.wibuplay.data.model.MediaGalleryItem
import com.dayynime.wibuplay.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val anime: AnimeItem? = null,
    val episodes: List<EpisodeItem> = emptyList(),
    val covers: List<MediaGalleryItem> = emptyList(),
    val posters: List<MediaGalleryItem> = emptyList(),
    val cuplix: List<CuplixItem> = emptyList(),
    val episodeSearch: String = "",
    val selectedTab: Int = 0, // 0 = Overview, 1 = List of Episodes, 2 = Cover / Poster / Cuplix
    val isLoading: Boolean = false,
    val isLoadingEpisodes: Boolean = false,
    val error: String? = null
)

class DetailViewModel(
    val movieId: String,
    private val repository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState(isLoading = true))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    val isFavorite: StateFlow<Boolean> = repository.isFavorite(movieId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        loadDetail()
        loadEpisodes()
        loadGallery()
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun onEpisodeSearchChange(query: String) {
        _uiState.update { it.copy(episodeSearch = query) }
        loadEpisodes(query)
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getMovieDetail(movieId).fold(
                onSuccess = { anime ->
                    _uiState.update { it.copy(anime = anime, isLoading = false) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(error = err.localizedMessage ?: "Gagal memuat detail anime", isLoading = false) }
                }
            )
        }
    }

    fun loadEpisodes(search: String = "") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingEpisodes = true) }
            repository.getMovieEpisodes(movieId, page = 0, search = search).fold(
                onSuccess = { list ->
                    _uiState.update { it.copy(episodes = list, isLoadingEpisodes = false) }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoadingEpisodes = false) }
                }
            )
        }
    }

    private fun loadGallery() {
        viewModelScope.launch {
            val covers = repository.getMovieCovers(movieId).getOrDefault(emptyList())
            val posters = repository.getMoviePosters(movieId).getOrDefault(emptyList())
            val cuplix = repository.getMovieCuplix(movieId).getOrDefault(emptyList())
            _uiState.update { it.copy(covers = covers, posters = posters, cuplix = cuplix) }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val anime = _uiState.value.anime ?: return@launch
            repository.toggleFavorite(anime)
        }
    }
}
