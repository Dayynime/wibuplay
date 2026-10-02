package com.dayynime.wibuplay.ui.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dayynime.wibuplay.data.model.AnimeItem
import com.dayynime.wibuplay.data.repository.AnimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ScheduleUiState(
    val selectedDay: String = "SENIN",
    val items: List<AnimeItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ScheduleViewModel(
    private val repository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ScheduleUiState(selectedDay = getTodayDayName())
    )
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    val days = listOf("SENIN", "SELASA", "RABU", "KAMIS", "JUMAT", "SABTU", "MINGGU")

    init {
        loadSchedule(_uiState.value.selectedDay)
    }

    private fun getTodayDayName(): String {
        return try {
            val sdf = SimpleDateFormat("EEEE", Locale("id", "ID"))
            val day = sdf.format(Date()).uppercase()
            when (day) {
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

    fun onDaySelected(day: String) {
        if (_uiState.value.selectedDay == day) return
        _uiState.update { it.copy(selectedDay = day) }
        loadSchedule(day)
    }

    fun loadSchedule(day: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getSchedule(day)
            result.fold(
                onSuccess = { list ->
                    _uiState.update { it.copy(items = list, isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.localizedMessage ?: "Gagal memuat jadwal rilis"
                        )
                    }
                }
            )
        }
    }
}
