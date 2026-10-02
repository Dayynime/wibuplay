package com.dayynime.wibuplay.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayynime.wibuplay.data.local.FavoriteEntity
import com.dayynime.wibuplay.data.local.WatchHistoryEntity
import com.dayynime.wibuplay.data.model.AnimeItem
import com.dayynime.wibuplay.data.model.HomeSectionData
import com.dayynime.wibuplay.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val sections: HomeSectionData) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

class HomeViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = repository.getAllWatchHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favorites: StateFlow<List<FavoriteEntity>> = repository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadHomeData()
    }

    private fun getCurrentDayName(): String {
        return try {
            val sdf = SimpleDateFormat("EEEE", Locale("id", "ID"))
            when (sdf.format(Date()).uppercase()) {
                "SENIN" -> "SENIN"
                "SELASA" -> "SELASA"
                "RABU" -> "RABU"
                "KAMIS" -> "KAMIS"
                "JUMAT", "JUM'AT" -> "JUMAT"
                "SABTU" -> "SABTU"
                "MINGGU" -> "MINGGU"
                else -> "SENIN"
            }
        } catch (_: Exception) {
            "SENIN"
        }
    }

    fun loadHomeData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!forceRefresh && _uiState.value is HomeUiState.Success) return@launch

            _uiState.value = HomeUiState.Loading
            val result = repository.getHomeSections(forceRefresh, getCurrentDayName())
            result.fold(
                onSuccess = { data ->
                    _uiState.value = HomeUiState.Success(data)
                },
                onFailure = { error ->
                    _uiState.value = HomeUiState.Error(error.localizedMessage ?: "Gagal memuat data beranda")
                }
            )
        }
    }
}
