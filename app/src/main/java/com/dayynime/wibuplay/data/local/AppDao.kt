package com.dayynime.wibuplay.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // Favorites
    @Query("SELECT * FROM favorites ORDER BY timestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :id)")
    fun isFavorite(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE id = :id")
    suspend fun deleteFavorite(id: String)

    // Watch History
    @Query("SELECT * FROM watch_history ORDER BY lastWatchedTime DESC")
    fun getAllWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE movieId = :movieId ORDER BY lastWatchedTime DESC LIMIT 1")
    fun getLatestHistoryForMovie(movieId: String): Flow<WatchHistoryEntity?>

    @Query("SELECT * FROM watch_history WHERE episodeId = :episodeId LIMIT 1")
    suspend fun getHistoryForEpisode(episodeId: String): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateHistory(history: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE id = :id")
    suspend fun deleteHistory(id: String)

    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()
}
