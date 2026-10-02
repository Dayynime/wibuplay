package com.dayynime.wibuplay.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dayynime.wibuplay.data.repository.AnimeRepository
import com.dayynime.wibuplay.ui.screens.cuplix.CuplixViewModel
import com.dayynime.wibuplay.ui.screens.detail.DetailViewModel
import com.dayynime.wibuplay.ui.screens.explore.ExploreViewModel
import com.dayynime.wibuplay.ui.screens.home.HomeViewModel
import com.dayynime.wibuplay.ui.screens.player.PlayerViewModel
import com.dayynime.wibuplay.ui.screens.profile.ProfileViewModel
import com.dayynime.wibuplay.ui.screens.schedule.ScheduleViewModel

class AppViewModelFactory(
    private val repository: AnimeRepository,
    private val extraString1: String = "",
    private val extraString2: String = ""
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ExploreViewModel::class.java) -> {
                ExploreViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ScheduleViewModel::class.java) -> {
                ScheduleViewModel(repository) as T
            }
            modelClass.isAssignableFrom(CuplixViewModel::class.java) -> {
                CuplixViewModel(repository) as T
            }
            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                ProfileViewModel(repository) as T
            }
            modelClass.isAssignableFrom(DetailViewModel::class.java) -> {
                DetailViewModel(extraString1, repository) as T
            }
            modelClass.isAssignableFrom(PlayerViewModel::class.java) -> {
                PlayerViewModel(extraString1, extraString2, repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
