package com.dayynime.wibuplay.ui.screens.cuplix

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayynime.wibuplay.data.model.CuplixItem
import com.dayynime.wibuplay.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

data class CuplixUiState(
    val items: List<CuplixItem> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isEndOfFeed: Boolean = false,
    val error: String? = null,
    val likedIds: Set<String> = emptySet()
)

class CuplixViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CuplixUiState())
    val uiState: StateFlow<CuplixUiState> = _uiState.asStateFlow()

    private val displayedIds = mutableSetOf<String>()
    private var currentCursors: Map<String, String> = emptyMap()
    val streamUrlCache = ConcurrentHashMap<String, String>() // episodeId -> direct stream url

    init {
        loadFeed(isInitial = true)
    }

    fun loadFeed(isInitial: Boolean = false) {
        viewModelScope.launch {
            if (_uiState.value.isEndOfFeed && !isInitial) return@launch
            _uiState.update {
                it.copy(
                    isLoading = isInitial,
                    isLoadingMore = !isInitial,
                    error = null
                )
            }

            val keyIdFyp = displayedIds.joinToString(",")
            val result = repository.getCuplixScroll(
                sort = "scroll_likes",
                keyIdFyp = keyIdFyp,
                cursorParams = currentCursors
            )

            result.fold(
                onSuccess = { (rawList, newCursors) ->
                    currentCursors = newCursors
                    // Filter: discard items without id_episode and dedupe by id
                    val filtered = rawList.filter { item ->
                        !item.id_episode.isNullOrBlank() && !displayedIds.contains(item.id)
                    }

                    if (filtered.isEmpty() && rawList.isNotEmpty()) {
                        // All items were duplicates -> feed finished
                        _uiState.update { it.copy(isLoading = false, isLoadingMore = false, isEndOfFeed = true) }
                        return@fold
                    }

                    filtered.forEach { displayedIds.add(it.id) }

                    _uiState.update { current ->
                        val combined = if (isInitial) filtered else current.items + filtered
                        current.copy(
                            items = combined,
                            isLoading = false,
                            isLoadingMore = false,
                            isEndOfFeed = filtered.isEmpty()
                        )
                    }

                    // Prefetch direct video links for first 2-3 items
                    filtered.take(3).forEach { item ->
                        item.id_episode?.let { epId -> prefetchStream(epId) }
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoadingMore = false,
                            error = if (isInitial) error.localizedMessage ?: "Gagal memuat Cuplix" else null
                        )
                    }
                }
            )
        }
    }

    fun onPageChanged(newIndex: Int) {
        _uiState.update { it.copy(currentIndex = newIndex) }
        val items = _uiState.value.items
        // Prefetch next 2 items
        for (i in (newIndex + 1)..(newIndex + 2)) {
            if (i < items.size) {
                items[i].id_episode?.let { prefetchStream(it) }
            }
        }
        // If near end, load more
        if (newIndex >= items.size - 3) {
            loadFeed(isInitial = false)
        }
    }

    fun toggleLike(id: String) {
        _uiState.update { state ->
            val newLiked = if (state.likedIds.contains(id)) {
                state.likedIds - id
            } else {
                state.likedIds + id
            }
            state.copy(likedIds = newLiked)
        }
    }

    private fun prefetchStream(episodeId: String) {
        if (streamUrlCache.containsKey(episodeId)) return
        viewModelScope.launch {
            repository.getEpisodeStream(episodeId).onSuccess { streamData ->
                val bestServer = findBestDirectServer(streamData.server)
                bestServer?.link?.let { link ->
                    streamUrlCache[episodeId] = link
                }
            }
        }
    }

    suspend fun resolveStreamUrl(episodeId: String): String? {
        val cached = streamUrlCache[episodeId]
        if (cached != null) return cached
        val res = repository.getEpisodeStream(episodeId)
        val streamData = res.getOrNull() ?: return null
        val bestServer = findBestDirectServer(streamData.server)
        val link = bestServer?.link
        if (link != null) {
            streamUrlCache[episodeId] = link
        }
        return link
    }

    fun invalidateStreamCache(episodeId: String) {
        streamUrlCache.remove(episodeId)
    }

    private fun findBestDirectServer(servers: List<com.dayynime.wibuplay.data.model.StreamServer>): com.dayynime.wibuplay.data.model.StreamServer? {
        if (servers.isEmpty()) return null
        // Pick direct server with quality closest to 480p
        val directServers = servers.filter { it.type?.equals("direct", ignoreCase = true) == true }
        val pool = if (directServers.isNotEmpty()) directServers else servers

        val p480 = pool.find { it.quality?.contains("480", ignoreCase = true) == true }
        if (p480 != null) return p480

        val p720 = pool.find { it.quality?.contains("720", ignoreCase = true) == true }
        if (p720 != null) return p720

        return pool.firstOrNull()
    }
}
