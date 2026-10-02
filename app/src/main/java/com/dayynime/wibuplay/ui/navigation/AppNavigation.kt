package com.dayynime.wibuplay.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dayynime.wibuplay.data.repository.AnimeRepository
import com.dayynime.wibuplay.ui.components.FloatingBottomBar
import com.dayynime.wibuplay.ui.components.LocalNavAnimatedScope
import com.dayynime.wibuplay.ui.components.LocalPosterTransitionHolder
import com.dayynime.wibuplay.ui.components.LocalSharedTransitionScope
import com.dayynime.wibuplay.ui.components.PosterTransitionHolder
import com.dayynime.wibuplay.ui.screens.cuplix.CuplixScreen
import com.dayynime.wibuplay.ui.screens.cuplix.CuplixViewModel
import com.dayynime.wibuplay.ui.screens.detail.DetailScreen
import com.dayynime.wibuplay.ui.screens.detail.DetailViewModel
import com.dayynime.wibuplay.ui.screens.explore.ExploreScreen
import com.dayynime.wibuplay.ui.screens.explore.ExploreViewModel
import com.dayynime.wibuplay.ui.screens.home.HomeScreen
import com.dayynime.wibuplay.ui.screens.home.HomeViewModel
import com.dayynime.wibuplay.ui.screens.player.PlayerScreen
import com.dayynime.wibuplay.ui.screens.player.PlayerViewModel
import com.dayynime.wibuplay.ui.screens.profile.ProfileScreen
import com.dayynime.wibuplay.ui.screens.profile.ProfileViewModel
import com.dayynime.wibuplay.ui.screens.schedule.ScheduleScreen
import com.dayynime.wibuplay.ui.screens.schedule.ScheduleViewModel
import com.dayynime.wibuplay.ui.theme.BackgroundDark

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    repository: AnimeRepository,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val posterHolder = remember { PosterTransitionHolder() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    // Tabs that should display the Floating Bottom Bar
    val isBottomBarVisible = when (currentDestination) {
        "home", "explore", "explore?category={category}&search={search}",
        "schedule", "cuplix", "profile" -> true
        else -> false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        val sharedScope = this
        CompositionLocalProvider(
            LocalSharedTransitionScope provides sharedScope,
            LocalPosterTransitionHolder provides posterHolder
        ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.fillMaxSize(),
            // Layar baru fade-in, layar lama diam di bawahnya (tidak ikut fade) -> tidak ada
            // "dip" gelap di tengah transisi, dan poster terbang di atas Beranda yang masih utuh.
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { fadeOut(tween(300)) }
        ) {
            // Home Screen
            composable(Screen.Home.route) {
                CompositionLocalProvider(LocalNavAnimatedScope provides this) {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = AppViewModelFactory(repository)
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onAnimeClick = { movieId ->
                        navController.navigate(Screen.Detail.createRoute(movieId))
                    },
                    onWatchEpisode = { movieId, episodeId ->
                        navController.navigate(Screen.Player.createRoute(movieId, episodeId))
                    },
                    onSearchClick = {
                        navController.navigate(Screen.Explore.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSeeAllClick = { category ->
                        navController.navigate(Screen.Explore.createRoute(category = category)) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
                }
            }

            // Explore Screen
            composable(
                route = Screen.Explore.route,
                arguments = listOf(
                    navArgument("category") {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument("search") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { backStackEntry ->
                val exploreViewModel: ExploreViewModel = viewModel(
                    factory = AppViewModelFactory(repository)
                )
                ExploreScreen(
                    viewModel = exploreViewModel,
                    onAnimeClick = { movieId ->
                        navController.navigate(Screen.Detail.createRoute(movieId))
                    }
                )
            }

            // Schedule Screen
            composable(Screen.Schedule.route) {
                val scheduleViewModel: ScheduleViewModel = viewModel(
                    factory = AppViewModelFactory(repository)
                )
                ScheduleScreen(
                    viewModel = scheduleViewModel,
                    onAnimeClick = { movieId ->
                        navController.navigate(Screen.Detail.createRoute(movieId))
                    }
                )
            }

            // Cuplix Screen
            composable(Screen.Cuplix.route) {
                val cuplixViewModel: CuplixViewModel = viewModel(
                    factory = AppViewModelFactory(repository)
                )
                CuplixScreen(
                    viewModel = cuplixViewModel,
                    onWatchAnime = { movieId, episodeId ->
                        navController.navigate(Screen.Player.createRoute(movieId, episodeId))
                    }
                )
            }

            // Profile Screen
            composable(Screen.Profile.route) {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = AppViewModelFactory(repository)
                )
                ProfileScreen(
                    viewModel = profileViewModel,
                    onAnimeClick = { movieId ->
                        navController.navigate(Screen.Detail.createRoute(movieId))
                    },
                    onWatchEpisode = { movieId, episodeId ->
                        navController.navigate(Screen.Player.createRoute(movieId, episodeId))
                    }
                )
            }

            // Anime Detail Screen
            composable(
                route = Screen.Detail.route,
                arguments = listOf(
                    navArgument("movieId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                CompositionLocalProvider(LocalNavAnimatedScope provides this) {
                val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
                val detailViewModel: DetailViewModel = viewModel(
                    key = "detail_$movieId",
                    factory = AppViewModelFactory(repository, extraString1 = movieId)
                )
                DetailScreen(
                    viewModel = detailViewModel,
                    onBackClick = { navController.popBackStack() },
                    onWatchEpisode = { mId, epId ->
                        navController.navigate(Screen.Player.createRoute(mId, epId))
                    }
                )
                }
            }

            // Video Player Screen
            composable(
                route = Screen.Player.route,
                arguments = listOf(
                    navArgument("movieId") { type = NavType.StringType },
                    navArgument("episodeId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getString("movieId") ?: ""
                val episodeId = backStackEntry.arguments?.getString("episodeId") ?: ""
                val playerViewModel: PlayerViewModel = viewModel(
                    key = "player_${movieId}_$episodeId",
                    factory = AppViewModelFactory(repository, extraString1 = movieId, extraString2 = episodeId)
                )
                PlayerScreen(
                    viewModel = playerViewModel,
                    onBackClick = { navController.popBackStack() },
                    onAnimeClick = { nextMovieId ->
                        navController.navigate(Screen.Detail.createRoute(nextMovieId))
                    }
                )
            }
        }
        }
        }

        // Floating Bottom Bar
        AnimatedVisibility(
            visible = isBottomBarVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val normalizedRoute = when {
                currentDestination?.startsWith("explore") == true -> "explore"
                else -> currentDestination ?: "home"
            }
            FloatingBottomBar(
                currentRoute = normalizedRoute,
                onTabSelected = { targetRoute ->
                    if (targetRoute != normalizedRoute) {
                        navController.navigate(targetRoute) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
