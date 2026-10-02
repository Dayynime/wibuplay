package com.dayynime.wibuplay.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayynime.wibuplay.data.local.FavoriteEntity
import com.dayynime.wibuplay.data.local.WatchHistoryEntity
import com.dayynime.wibuplay.data.repository.AnimeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    val favorites: StateFlow<List<FavoriteEntity>> = repository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = repository.getAllWatchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeFavorite(id: String) {
        viewModelScope.launch {
            repository.removeFavorite(id)
        }
    }

    fun deleteHistory(id: String) {
        viewModelScope.launch {
            repository.deleteHistory(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }
}
