package com.dayynime.wibuplay.data.api

import com.dayynime.wibuplay.data.model.AnimeItem
import com.dayynime.wibuplay.data.model.CuplixItem
import com.dayynime.wibuplay.data.model.EpisodeItem
import com.dayynime.wibuplay.data.model.GenreItem
import com.dayynime.wibuplay.data.model.MediaGalleryItem
import com.dayynime.wibuplay.data.model.StreamData
import com.dayynime.wibuplay.data.model.StreamServer
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object JsonHelper {
    val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    fun parseAnimeItem(map: Map<*, *>?): AnimeItem? {
        if (map == null) return null
        return try {
            AnimeItem(
                id = map["id"]?.toString(),
                title = map["title"]?.toString() ?: map["anime"]?.toString(),
                synopsis = map["synopsis"]?.toString(),
                genre = map["genre"]?.toString(),
                status = map["status"]?.toString(),
                type = map["type"]?.toString(),
                year = map["year"]?.toString(),
                day = map["day"]?.toString(),
                views = map["views"]?.toString() ?: map["count_views"]?.toString(),
                image_poster = map["image_poster"]?.toString() ?: map["poster"]?.toString() ?: map["image"]?.toString(),
                image_cover = map["image_cover"]?.toString() ?: map["cover"]?.toString(),
                studio = map["studio"]?.toString(),
                aired_start = map["aired_start"]?.toString(),
                aired_end = map["aired_end"]?.toString()
            )
        } catch (_: Exception) {
            null
        }
    }

    fun parseAnimeList(data: Any?): List<AnimeItem> {
        if (data == null) return emptyList()
        val list = when (data) {
            is List<*> -> data
            is Map<*, *> -> {
                // If it's a map, maybe under "movie" or "movies" or "data"
                val movie = data["movie"] ?: data["movies"] ?: data["data"]
                if (movie is List<*>) movie else emptyList<Any>()
            }
            else -> emptyList<Any>()
        }
        return list.mapNotNull { item ->
            if (item is Map<*, *>) parseAnimeItem(item) else null
        }
    }

    fun parseGenreList(data: Any?): List<GenreItem> {
        if (data == null) return emptyList()
        val list = when (data) {
            is List<*> -> data
            is Map<*, *> -> {
                val genre = data["genre"] ?: data["genres"] ?: data["data"]
                if (genre is List<*>) genre else emptyList<Any>()
            }
            else -> emptyList<Any>()
        }
        return list.mapNotNull { item ->
            if (item is Map<*, *>) {
                GenreItem(
                    id = item["id"]?.toString(),
                    name = item["name"]?.toString() ?: item["title"]?.toString(),
                    total = item["total"]?.toString()
                )
            } else null
        }
    }

    fun parseEpisodeItem(map: Map<*, *>?): EpisodeItem? {
        if (map == null) return null
        return try {
            EpisodeItem(
                id = map["id"]?.toString(),
                index = map["index"]?.toString() ?: map["episode"]?.toString(),
                image = map["image"]?.toString() ?: map["thumbnail"]?.toString(),
                title = map["title"]?.toString() ?: "Episode ${map["index"] ?: map["episode"] ?: ""}",
                views = map["views"]?.toString(),
                is_new = map["is_new"]
            )
        } catch (_: Exception) {
            null
        }
    }

    fun parseEpisodeList(data: Any?): List<EpisodeItem> {
        if (data == null) return emptyList()
        val list = when (data) {
            is List<*> -> data
            is Map<*, *> -> {
                val ep = data["episode"] ?: data["episodes"] ?: data["data"]
                if (ep is List<*>) ep else emptyList<Any>()
            }
            else -> emptyList<Any>()
        }
        return list.mapNotNull { item ->
            if (item is Map<*, *>) parseEpisodeItem(item) else null
        }
    }

    fun parseStreamData(data: Any?): StreamData {
        if (data !is Map<*, *>) return StreamData()

        val episodeMap = data["episode"] as? Map<*, *>
        val episode = parseEpisodeItem(episodeMap)

        val nextEpRaw = data["episode_next"]
        val (nextEpisode, hasNext) = when (nextEpRaw) {
            is Map<*, *> -> Pair(parseEpisodeItem(nextEpRaw), true)
            is Boolean -> Pair(null, nextEpRaw)
            else -> Pair(null, false)
        }

        val serversRaw = data["server"]
        val servers = if (serversRaw is List<*>) {
            serversRaw.mapNotNull { s ->
                if (s is Map<*, *>) {
                    StreamServer(
                        link = s["link"]?.toString(),
                        quality = s["quality"]?.toString(),
                        type = s["type"]?.toString(),
                        name = s["name"]?.toString() ?: s["title"]?.toString()
                    )
                } else null
            }
        } else {
            emptyList()
        }

        return StreamData(
            episode = episode,
            episode_next = nextEpisode,
            hasNextEpisode = hasNext,
            server = servers
        )
    }

    fun parseCuplixList(data: Any?): List<CuplixItem> {
        if (data == null) return emptyList()
        val list = when (data) {
            is List<*> -> data
            is Map<*, *> -> {
                val fyp = data["list"] ?: data["fyp"] ?: data["data"]
                if (fyp is List<*>) fyp else emptyList<Any>()
            }
            else -> emptyList<Any>()
        }
        return list.mapNotNull { item ->
            if (item is Map<*, *>) {
                val id = item["id"]?.toString() ?: return@mapNotNull null
                CuplixItem(
                    id = id,
                    caption = item["caption"]?.toString(),
                    url_thumbnail = item["url_thumbnail"]?.toString() ?: item["thumbnail"]?.toString(),
                    id_episode = item["id_episode"]?.toString(),
                    id_movie = item["id_movie"]?.toString(),
                    anime = item["anime"]?.toString(),
                    episode = item["episode"]?.toString(),
                    time_start = item["time_start"]?.toString(),
                    time_end = item["time_end"]?.toString(),
                    count_views = item["count_views"]?.toString() ?: item["views"]?.toString(),
                    count_likes = item["count_likes"]?.toString() ?: item["likes"]?.toString(),
                    count_comments = item["count_comments"]?.toString() ?: item["comments"]?.toString(),
                    username = item["username"]?.toString()
                )
            } else null
        }
    }

    fun parseMediaList(data: Any?): List<MediaGalleryItem> {
        if (data == null) return emptyList()
        val list = when (data) {
            is List<*> -> data
            is Map<*, *> -> {
                val items = data["data"] ?: data["list"] ?: data["poster"] ?: data["cover"]
                if (items is List<*>) items else emptyList<Any>()
            }
            else -> emptyList<Any>()
        }
        return list.mapNotNull { item ->
            if (item is Map<*, *>) {
                MediaGalleryItem(
                    id = item["id"]?.toString(),
                    image = item["image"]?.toString() ?: item["url"]?.toString() ?: item["cover"]?.toString(),
                    title = item["title"]?.toString()
                )
            } else null
        }
    }

    /**
     * Format cursor values: clean Double (e.g. 77.0 -> 77)
     */
    fun sanitizeCursorValue(value: Any?): String {
        if (value == null) return ""
        return when (value) {
            is Double -> {
                if (value == value.toLong().toDouble()) {
                    value.toLong().toString()
                } else {
                    value.toString()
                }
            }
            is Float -> {
                if (value == value.toLong().toFloat()) {
                    value.toLong().toString()
                } else {
                    value.toString()
                }
            }
            else -> value.toString()
        }
    }

    /**
     * Extracts cursor_* keys from a data map and returns a map of clean string values
     */
    fun extractCursors(data: Any?): Map<String, String> {
        if (data !is Map<*, *>) return emptyMap()
        val cursors = mutableMapOf<String, String>()
        for ((k, v) in data) {
            val key = k?.toString() ?: continue
            if (key.startsWith("cursor_")) {
                cursors[key] = sanitizeCursorValue(v)
            }
        }
        return cursors
    }
}
