package com.dayynime.wibuplay.data.repository

import com.dayynime.wibuplay.data.api.ApiService
import com.dayynime.wibuplay.data.api.JsonHelper
import com.dayynime.wibuplay.data.local.AppDao
import com.dayynime.wibuplay.data.local.FavoriteEntity
import com.dayynime.wibuplay.data.local.WatchHistoryEntity
import com.dayynime.wibuplay.data.model.AnimeItem
import com.dayynime.wibuplay.data.model.CuplixItem
import com.dayynime.wibuplay.data.model.EpisodeItem
import com.dayynime.wibuplay.data.model.GenreItem
import com.dayynime.wibuplay.data.model.HomeSectionData
import com.dayynime.wibuplay.data.model.MediaGalleryItem
import com.dayynime.wibuplay.data.model.StreamData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class AnimeRepository(
    private val apiService: ApiService,
    private val appDao: AppDao
) {
    // In-memory cache
    private var cachedHome: HomeSectionData? = null
    private var cachedHomeTime: Long = 0
    private val detailCache = ConcurrentHashMap<String, AnimeItem>()
    private var cachedGenres: List<GenreItem>? = null

    private fun filterValidHeroAnime(list: List<AnimeItem>): List<AnimeItem> {
        return list.filter { item ->
            val hasImage = (!item.image_cover.isNullOrBlank() || !item.image_poster.isNullOrBlank())
            val hasTitle = !item.title.isNullOrBlank() &&
                !item.title.equals("NONE", ignoreCase = true) &&
                !item.title.equals("Anime", ignoreCase = true)
            hasImage && hasTitle
        }.take(6)
    }

    suspend fun getHomeSections(forceRefresh: Boolean = false, currentDay: String = "SENIN"): Result<HomeSectionData> =
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            if (!forceRefresh && cachedHome != null && (now - cachedHomeTime < 5 * 60 * 1000)) {
                return@withContext Result.success(cachedHome!!)
            }

            try {
                // Fetch hot category from 3/2/home/hot to guarantee real anime for hero carousel
                var heroAnimeList: List<AnimeItem> = emptyList()
                try {
                    val hotApiResp = apiService.getHomeCategory("hot", 0)
                    val hotApiList = JsonHelper.parseAnimeList(hotApiResp.data)
                    heroAnimeList = filterValidHeroAnime(hotApiList)
                } catch (_: Exception) {}

                // Try data/home/list
                val resp = apiService.getHomeList(limit = 12, day = currentDay)
                val rawData = resp.data
                if (rawData is Map<*, *>) {
                    val hot = JsonHelper.parseAnimeList(rawData["hot"])
                    val popular = JsonHelper.parseAnimeList(rawData["popular"])
                    val newRelease = JsonHelper.parseAnimeList(rawData["new"])
                    val random = JsonHelper.parseAnimeList(rawData["random"])
                    val today = JsonHelper.parseAnimeList(rawData["today"])
                    val update = JsonHelper.parseAnimeList(rawData["update"])

                    // If heroAnimeList was empty, take from hot or popular (NEVER rawData["slider"])
                    if (heroAnimeList.isEmpty()) {
                        heroAnimeList = filterValidHeroAnime(hot)
                    }
                    if (heroAnimeList.isEmpty()) {
                        heroAnimeList = filterValidHeroAnime(popular)
                    }

                    val result = HomeSectionData(
                        slider = heroAnimeList,
                        hot = hot,
                        popular = popular,
                        newRelease = newRelease,
                        random = random,
                        today = today,
                        update = update
                    )
                    cachedHome = result
                    cachedHomeTime = now
                    return@withContext Result.success(result)
                }

                // Fallback to individual categories
                val hotResp = apiService.getHomeCategory("hot", 0)
                val popResp = apiService.getHomeCategory("popular", 0)
                val newResp = apiService.getHomeCategory("new", 0)
                val ranResp = apiService.getHomeCategory("random", 0)

                val hot = JsonHelper.parseAnimeList(hotResp.data)
                val popular = JsonHelper.parseAnimeList(popResp.data)
                val newRelease = JsonHelper.parseAnimeList(newResp.data)
                val random = JsonHelper.parseAnimeList(ranResp.data)

                if (heroAnimeList.isEmpty()) {
                    heroAnimeList = filterValidHeroAnime(hot).ifEmpty { filterValidHeroAnime(popular) }
                }

                val result = HomeSectionData(
                    slider = heroAnimeList,
                    hot = hot,
                    popular = popular,
                    newRelease = newRelease,
                    random = random,
                    today = emptyList(),
                    update = emptyList()
                )
                cachedHome = result
                cachedHomeTime = now
                Result.success(result)
            } catch (e: Exception) {
                // Return cache if available even if expired, else failure
                if (cachedHome != null) {
                    Result.success(cachedHome!!)
                } else {
                    Result.failure(e)
                }
            }
        }

    suspend fun getHomeCategory(category: String, page: Int): Result<List<AnimeItem>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = apiService.getHomeCategory(category, page)
                val list = JsonHelper.parseAnimeList(resp.data)
                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getSchedule(day: String, page: Int = 0): Result<List<AnimeItem>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = apiService.getSchedule(day = day, sort = "views", page = page)
                val list = JsonHelper.parseAnimeList(resp.data)
                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getGenres(): Result<List<GenreItem>> =
        withContext(Dispatchers.IO) {
            if (cachedGenres != null && cachedGenres!!.isNotEmpty()) {
                return@withContext Result.success(cachedGenres!!)
            }
            try {
                val resp = apiService.getGenreList()
                val list = JsonHelper.parseGenreList(resp.data)
                cachedGenres = list
                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun explore(
        keyword: String = "",
        genreId: String? = null,
        type: String? = null,
        year: String? = null,
        studio: String? = null,
        sort: String = "views",
        page: Int = 0
    ): Result<List<AnimeItem>> = withContext(Dispatchers.IO) {
        try {
            val resp = when {
                !genreId.isNullOrBlank() -> apiService.exploreByGenre(genreId, sort, page)
                !type.isNullOrBlank() -> apiService.exploreByType(type, sort, page)
                !year.isNullOrBlank() -> apiService.exploreByYear(year, "", sort, page)
                !studio.isNullOrBlank() -> apiService.exploreByStudio(studio, sort, page)
                else -> apiService.exploreMovie(keyword, sort, page)
            }
            val list = JsonHelper.parseAnimeList(resp.data)
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMovieDetail(id: String, forceRefresh: Boolean = false): Result<AnimeItem> =
        withContext(Dispatchers.IO) {
            if (!forceRefresh) {
                val cached = detailCache[id]
                if (cached != null) return@withContext Result.success(cached)
            }
            try {
                val resp = apiService.getMovieDetail(id)
                val data = resp.data
                val animeItem = if (data is Map<*, *>) {
                    val movieObj = data["movie"] as? Map<*, *> ?: data
                    JsonHelper.parseAnimeItem(movieObj)
                } else null

                if (animeItem != null) {
                    detailCache[id] = animeItem
                    Result.success(animeItem)
                } else {
                    Result.failure(Exception("Format detail anime tidak dikenali"))
                }
            } catch (e: Exception) {
                val cached = detailCache[id]
                if (cached != null) Result.success(cached) else Result.failure(e)
            }
        }

    suspend fun getMovieEpisodes(id: String, page: Int = 0, search: String = ""): Result<List<EpisodeItem>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = apiService.getMovieEpisodes(id, page, search)
                val list = JsonHelper.parseEpisodeList(resp.data)
                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getEpisodeStream(idEpisode: String): Result<StreamData> =
        withContext(Dispatchers.IO) {
            try {
                val resp = apiService.getEpisodeStream(idEpisode)
                val stream = JsonHelper.parseStreamData(resp.data)
                Result.success(stream)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getMovieCovers(idMovie: String, page: Int = 0): Result<List<MediaGalleryItem>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = apiService.getMovieCovers(idMovie, page)
                val list = JsonHelper.parseMediaList(resp.data)
                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getMoviePosters(idMovie: String, page: Int = 0): Result<List<MediaGalleryItem>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = apiService.getMoviePosters(idMovie, page)
                val list = JsonHelper.parseMediaList(resp.data)
                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getMovieCuplix(idMovie: String, page: Int = 0): Result<List<CuplixItem>> =
        withContext(Dispatchers.IO) {
            try {
                val resp = apiService.getMovieCuplix(idMovie, page)
                val list = JsonHelper.parseCuplixList(resp.data)
                Result.success(list)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getCuplixScroll(
        sort: String = "scroll_likes",
        keyIdFyp: String = "",
        cursorParams: Map<String, String> = emptyMap()
    ): Result<Pair<List<CuplixItem>, Map<String, String>>> = withContext(Dispatchers.IO) {
        try {
            val resp = apiService.getCuplixScroll(
                limit = 30,
                sort = sort,
                keyIdFyp = keyIdFyp,
                cursorParams = cursorParams
            )
            val list = JsonHelper.parseCuplixList(resp.data)
            val newCursors = JsonHelper.extractCursors(resp.data)
            Result.success(Pair(list, newCursors))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Room Database Operations
    fun getAllFavorites(): Flow<List<FavoriteEntity>> = appDao.getAllFavorites()

    fun isFavorite(id: String): Flow<Boolean> = appDao.isFavorite(id)

    suspend fun toggleFavorite(anime: AnimeItem) = withContext(Dispatchers.IO) {
        val animeId = anime.id ?: return@withContext
        val isFav = appDao.isFavorite(animeId).firstOrNull() ?: false
        if (isFav) {
            appDao.deleteFavorite(animeId)
        } else {
            appDao.insertFavorite(
                FavoriteEntity(
                    id = animeId,
                    title = anime.title ?: "Anime",
                    posterUrl = anime.getPosterUrl(),
                    coverUrl = anime.getCoverUrl(),
                    synopsis = anime.synopsis,
                    genre = anime.genre,
                    status = anime.status,
                    type = anime.type,
                    views = anime.views
                )
            )
        }
    }

    suspend fun removeFavorite(id: String) = withContext(Dispatchers.IO) {
        appDao.deleteFavorite(id)
    }

    fun getAllWatchHistory(): Flow<List<WatchHistoryEntity>> = appDao.getAllWatchHistory()

    fun getLatestHistoryForMovie(movieId: String): Flow<WatchHistoryEntity?> = appDao.getLatestHistoryForMovie(movieId)

    suspend fun getHistoryForEpisode(episodeId: String): WatchHistoryEntity? = withContext(Dispatchers.IO) {
        appDao.getHistoryForEpisode(episodeId)
    }

    suspend fun saveWatchProgress(
        movieId: String,
        movieTitle: String,
        moviePoster: String,
        episodeId: String,
        episodeIndex: String,
        episodeTitle: String,
        playbackPositionMs: Long,
        durationMs: Long
    ) = withContext(Dispatchers.IO) {
        if (episodeId.isBlank() || movieId.isBlank()) return@withContext
        val history = WatchHistoryEntity(
            id = "${movieId}_${episodeId}",
            movieId = movieId,
            movieTitle = movieTitle,
            moviePoster = moviePoster,
            episodeId = episodeId,
            episodeIndex = episodeIndex,
            episodeTitle = episodeTitle,
            playbackPositionMs = playbackPositionMs,
            durationMs = durationMs,
            lastWatchedTime = System.currentTimeMillis()
        )
        appDao.insertOrUpdateHistory(history)
    }

    suspend fun deleteHistory(id: String) = withContext(Dispatchers.IO) {
        appDao.deleteHistory(id)
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        appDao.clearAllHistory()
    }
}
