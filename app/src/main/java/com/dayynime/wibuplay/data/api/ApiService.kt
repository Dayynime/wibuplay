package com.dayynime.wibuplay.data.api

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

@JsonClass(generateAdapter = true)
data class ApiResponseWrapper(
    val status: Int? = null,
    val error: Boolean? = null,
    val data: Any? = null
)

interface ApiService {

    // Beranda
    @GET("3/2/home/{category}")
    suspend fun getHomeCategory(
        @Path("category") category: String, // hot, new, popular, random
        @Query("page") page: Int = 0
    ): ApiResponseWrapper

    @GET("data/home/list")
    suspend fun getHomeList(
        @Query("limit") limit: Int = 10,
        @Query("day") day: String = "SENIN"
    ): ApiResponseWrapper

    @GET("3/2/schedule/data")
    suspend fun getSchedule(
        @Query("day") day: String,
        @Query("sort") sort: String = "views",
        @Query("page") page: Int = 0
    ): ApiResponseWrapper

    // Cari & Jelajah
    @GET("3/2/explore/movie")
    suspend fun exploreMovie(
        @Query("keyword") keyword: String = "",
        @Query("sort") sort: String = "views",
        @Query("page") page: Int = 0
    ): ApiResponseWrapper

    @GET("3/2/explore/movie_genre")
    suspend fun exploreByGenre(
        @Query("id_genre") idGenre: String,
        @Query("sort") sort: String = "views",
        @Query("page") page: Int = 0
    ): ApiResponseWrapper

    @GET("3/2/explore/movie_type")
    suspend fun exploreByType(
        @Query("type") type: String,
        @Query("sort") sort: String = "views",
        @Query("page") page: Int = 0
    ): ApiResponseWrapper

    @GET("3/2/explore/movie_year")
    suspend fun exploreByYear(
        @Query("year") year: String,
        @Query("season") season: String = "",
        @Query("sort") sort: String = "views",
        @Query("page") page: Int = 0
    ): ApiResponseWrapper

    @GET("3/2/explore/movie_studio")
    suspend fun exploreByStudio(
        @Query("studio") studio: String,
        @Query("sort") sort: String = "views",
        @Query("page") page: Int = 0
    ): ApiResponseWrapper

    @GET("3/2/explore/genre")
    suspend fun getGenreList(): ApiResponseWrapper

    // Detail & Streaming
    @GET("3/2/movie/detail/{id}")
    suspend fun getMovieDetail(
        @Path("id") id: String
    ): ApiResponseWrapper

    @GET("3/2/movie/episode/{id}")
    suspend fun getMovieEpisodes(
        @Path("id") id: String,
        @Query("page") page: Int = 0,
        @Query("search") search: String = ""
    ): ApiResponseWrapper

    @GET("3/2/episode/streamnew/{id_episode}")
    suspend fun getEpisodeStream(
        @Path("id_episode") idEpisode: String
    ): ApiResponseWrapper

    // Tab Detail
    @GET("3/2/movie_cover/data")
    suspend fun getMovieCovers(
        @Query("id_movie") idMovie: String,
        @Query("page") page: Int = 0
    ): ApiResponseWrapper

    @GET("3/2/movie_poster/data")
    suspend fun getMoviePosters(
        @Query("id_movie") idMovie: String,
        @Query("page") page: Int = 0
    ): ApiResponseWrapper

    @GET("data/movie/fyp/list_new")
    suspend fun getMovieCuplix(
        @Query("id_movie") idMovie: String,
        @Query("page") page: Int = 0,
        @Query("type") type: String = "NEW"
    ): ApiResponseWrapper

    // Cuplix Global
    @GET("data/fyp2/list_scroll")
    suspend fun getCuplixScroll(
        @Query("limit") limit: Int = 30,
        @Query("sort") sort: String = "scroll_likes", // scroll_likes | scroll_new | scroll_old
        @Query("key_id_fyp") keyIdFyp: String = "",
        @QueryMap cursorParams: Map<String, String> = emptyMap()
    ): ApiResponseWrapper
}
