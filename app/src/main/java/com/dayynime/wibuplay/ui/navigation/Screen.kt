package com.dayynime.wibuplay.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Explore : Screen("explore?category={category}&search={search}") {
        fun createRoute(category: String = "", search: String = ""): String {
            return "explore?category=$category&search=$search"
        }
    }
    object Schedule : Screen("schedule")
    object Cuplix : Screen("cuplix")
    object Profile : Screen("profile")
    object Detail : Screen("detail/{movieId}") {
        fun createRoute(movieId: String): String = "detail/$movieId"
    }
    object Player : Screen("player/{movieId}/{episodeId}") {
        fun createRoute(movieId: String, episodeId: String): String = "player/$movieId/$episodeId"
    }
}
