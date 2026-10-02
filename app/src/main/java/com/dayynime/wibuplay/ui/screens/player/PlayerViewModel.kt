package com.dayynime.wibuplay.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayynime.wibuplay.data.model.AnimeItem
import com.dayynime.wibuplay.data.model.EpisodeItem
import com.dayynime.wibuplay.data.model.StreamData
import com.dayynime.wibuplay.data.model.StreamServer
import com.dayynime.wibuplay.data.repository.AnimeRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val movieId: String = "",
    val currentEpisodeId: String = "",
    val anime: AnimeItem? = null,
    val episodes: List<EpisodeItem> = emptyList(),
    val streamData: StreamData? = null,
    val selectedServer: StreamServer? = null,
    val recommended: List<AnimeItem> = emptyList(),
    val isLoadingStream: Boolean = false,
    val streamError: String? = null,
    val isFullscreen: Boolean = false,
    val autoNextCountdown: Int? = null, // null when not counting down
    val resumePositionMs: Long = 0L
)

class PlayerViewModel(
    val initialMovieId: String,
    val initialEpisodeId: String,
    private val repository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PlayerUiState(
            movieId = initialMovieId,
            currentEpisodeId = initialEpisodeId
        )
    )
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val isFavorite: StateFlow<Boolean> = repository.isFavorite(initialMovieId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var countdownJob: Job? = null
    private var progressSaveJob: Job? = null

    init {
        loadAnimeInfo()
        loadEpisodes()
        loadRecommendations()
        if (initialEpisodeId.isNotBlank()) {
            loadEpisodeStream(initialEpisodeId)
        }
    }

    private fun loadAnimeInfo() {
        viewModelScope.launch {
            repository.getMovieDetail(_uiState.value.movieId).onSuccess { anime ->
                _uiState.update { it.copy(anime = anime) }
            }
        }
    }

    private fun loadEpisodes() {
        viewModelScope.launch {
            repository.getMovieEpisodes(_uiState.value.movieId, page = 0).onSuccess { eps ->
                _uiState.update { it.copy(episodes = eps) }
                // If initial episode id was empty, play the first one
                if (_uiState.value.currentEpisodeId.isBlank() && eps.isNotEmpty()) {
                    val firstEp = eps.first()
                    firstEp.id?.let { loadEpisodeStream(it) }
                }
            }
        }
    }

    private fun loadRecommendations() {
        viewModelScope.launch {
            repository.getHomeCategory("random", page = 0).onSuccess { list ->
                _uiState.update { it.copy(recommended = list.take(8)) }
            }
        }
    }

    fun loadEpisodeStream(episodeId: String) {
        cancelAutoNext()
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    currentEpisodeId = episodeId,
                    isLoadingStream = true,
                    streamError = null
                )
            }

            // Check saved progress from Room
            val lastHistory = repository.getHistoryForEpisode(episodeId)
            val resumeMs = lastHistory?.playbackPositionMs ?: 0L

            val result = repository.getEpisodeStream(episodeId)
            result.fold(
                onSuccess = { data ->
                    val defaultServer = data.server.firstOrNull { it.link?.isNotBlank() == true }
                    _uiState.update {
                        it.copy(
                            streamData = data,
                            selectedServer = defaultServer,
                            isLoadingStream = false,
                            resumePositionMs = resumeMs
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingStream = false,
                            streamError = err.localizedMessage ?: "Gagal memuat server video"
                        )
                    }
                }
            )
        }
    }

    fun selectServer(server: StreamServer) {
        _uiState.update { it.copy(selectedServer = server, streamError = null) }
    }

    fun setFullscreen(fullscreen: Boolean) {
        _uiState.update { it.copy(isFullscreen = fullscreen) }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val anime = _uiState.value.anime ?: return@launch
            repository.toggleFavorite(anime)
        }
    }

    fun saveProgress(positionMs: Long, durationMs: Long) {
        val state = _uiState.value
        val ep = state.episodes.find { it.id == state.currentEpisodeId } ?: state.streamData?.episode
        val anime = state.anime
        if (state.currentEpisodeId.isNotBlank() && anime != null) {
            viewModelScope.launch {
                repository.saveWatchProgress(
                    movieId = state.movieId,
                    movieTitle = anime.title ?: "Anime",
                    moviePoster = anime.getPosterUrl(),
                    episodeId = state.currentEpisodeId,
                    episodeIndex = ep?.index ?: "1",
                    episodeTitle = ep?.title ?: "Episode",
                    playbackPositionMs = positionMs,
                    durationMs = durationMs
                )
            }
        }
    }

    fun startAutoNextCountdown(onTriggerNext: () -> Unit) {
        val nextEp = _uiState.value.streamData?.episode_next
        val hasNext = _uiState.value.streamData?.hasNextEpisode == true || nextEp != null
        if (!hasNext) return

        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            for (i in 5 downTo 1) {
                _uiState.update { it.copy(autoNextCountdown = i) }
                delay(1000)
            }
            _uiState.update { it.copy(autoNextCountdown = null) }
            // Play next episode
            val nextEpId = nextEp?.id ?: findNextEpisodeIdInList()
            if (!nextEpId.isNullOrBlank()) {
                loadEpisodeStream(nextEpId)
                onTriggerNext()
            }
        }
    }

    fun cancelAutoNext() {
        countdownJob?.cancel()
        countdownJob = null
        _uiState.update { it.copy(autoNextCountdown = null) }
    }

    private fun findNextEpisodeIdInList(): String? {
        val list = _uiState.value.episodes
        val currentIdx = list.indexOfFirst { it.id == _uiState.value.currentEpisodeId }
        return if (currentIdx in 0 until (list.size - 1)) {
            list[currentIdx + 1].id
        } else null
    }
}
