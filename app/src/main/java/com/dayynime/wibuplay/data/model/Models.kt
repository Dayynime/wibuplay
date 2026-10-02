package com.dayynime.wibuplay.data.model

import com.dayynime.wibuplay.data.api.Constants

data class ApiResponse(
    val status: Int? = null,
    val error: Boolean? = null,
    val data: Any? = null
)

data class AnimeItem(
    val id: String? = null,
    val title: String? = null,
    val synopsis: String? = null,
    val genre: String? = null,
    val status: String? = null,
    val type: String? = null,
    val year: String? = null,
    val day: String? = null,
    val views: String? = null,
    val image_poster: String? = null,
    val image_cover: String? = null,
    val studio: String? = null,
    val aired_start: String? = null,
    val aired_end: String? = null
) {
    fun getPosterUrl(): String {
        return buildFullUrl(image_poster)
    }

    fun getCoverUrl(): String {
        return buildFullUrl(image_cover ?: image_poster)
    }

    private fun buildFullUrl(path: String?): String {
        if (path.isNullOrBlank()) return ""
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path
        }
        val cleanBase = Constants.BASE_URL.removeSuffix("/")
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return cleanBase + cleanPath
    }
}

data class GenreItem(
    val id: String? = null,
    val name: String? = null,
    val total: String? = null
)

data class EpisodeItem(
    val id: String? = null,
    val index: String? = null,
    val image: String? = null,
    val title: String? = null,
    val views: String? = null,
    val is_new: Any? = null
) {
    fun getImageUrl(): String {
        if (image.isNullOrBlank()) return ""
        if (image.startsWith("http://") || image.startsWith("https://")) {
            return image
        }
        val cleanBase = Constants.BASE_URL.removeSuffix("/")
        val cleanPath = if (image.startsWith("/")) image else "/$image"
        return cleanBase + cleanPath
    }
}

data class StreamServer(
    val link: String? = null,
    val quality: String? = null,
    val type: String? = null,
    val name: String? = null
)

data class StreamData(
    val episode: EpisodeItem? = null,
    val episode_next: EpisodeItem? = null,
    val hasNextEpisode: Boolean = false,
    val server: List<StreamServer> = emptyList()
)

data class CuplixItem(
    val id: String = "",
    val caption: String? = null,
    val url_thumbnail: String? = null,
    val id_episode: String? = null,
    val id_movie: String? = null,
    val anime: String? = null,
    val episode: String? = null,
    val time_start: String? = null,
    val time_end: String? = null,
    val count_views: String? = null,
    val count_likes: String? = null,
    val count_comments: String? = null,
    val username: String? = null
) {
    fun getThumbnailUrl(): String {
        if (url_thumbnail.isNullOrBlank()) return ""
        if (url_thumbnail.startsWith("http://") || url_thumbnail.startsWith("https://")) {
            return url_thumbnail
        }
        val cleanBase = Constants.BASE_URL.removeSuffix("/")
        val cleanPath = if (url_thumbnail.startsWith("/")) url_thumbnail else "/$url_thumbnail"
        return cleanBase + cleanPath
    }

    fun getTimeStartMs(): Long {
        return time_start?.toLongOrNull() ?: 0L
    }

    fun getTimeEndMs(): Long {
        return time_end?.toLongOrNull() ?: 0L
    }
}

data class MediaGalleryItem(
    val id: String? = null,
    val image: String? = null,
    val title: String? = null
) {
    fun getFullImageUrl(): String {
        if (image.isNullOrBlank()) return ""
        if (image.startsWith("http://") || image.startsWith("https://")) return image
        val cleanBase = Constants.BASE_URL.removeSuffix("/")
        val cleanPath = if (image.startsWith("/")) image else "/$image"
        return cleanBase + cleanPath
    }
}

data class HomeSectionData(
    val slider: List<AnimeItem> = emptyList(),
    val hot: List<AnimeItem> = emptyList(),
    val popular: List<AnimeItem> = emptyList(),
    val newRelease: List<AnimeItem> = emptyList(),
    val random: List<AnimeItem> = emptyList(),
    val today: List<AnimeItem> = emptyList(),
    val update: List<AnimeItem> = emptyList()
)
