package com.dayynime.wibuplay.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dayynime.wibuplay.data.model.AnimeItem

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val posterUrl: String,
    val coverUrl: String,
    val synopsis: String? = null,
    val genre: String? = null,
    val status: String? = null,
    val type: String? = null,
    val views: String? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toAnimeItem(): AnimeItem {
        return AnimeItem(
            id = id,
            title = title,
            synopsis = synopsis,
            genre = genre,
            status = status,
            type = type,
            views = views,
            image_poster = posterUrl,
            image_cover = coverUrl
        )
    }
}

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey
    val id: String, // e.g. "${movieId}_${episodeId}"
    val movieId: String,
    val movieTitle: String,
    val moviePoster: String,
    val episodeId: String,
    val episodeIndex: String,
    val episodeTitle: String,
    val playbackPositionMs: Long,
    val durationMs: Long,
    val lastWatchedTime: Long = System.currentTimeMillis()
) {
    val progressFraction: Float
        get() = if (durationMs > 0) (playbackPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
