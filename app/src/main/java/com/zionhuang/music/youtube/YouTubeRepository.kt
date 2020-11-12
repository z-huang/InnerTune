package com.zionhuang.music.youtube

import android.content.Context
import com.google.api.services.youtube.model.SearchListResponse
import com.google.api.services.youtube.model.VideoListResponse

class YouTubeRepository private constructor(context: Context) {

    private val youTubeAPIService: YouTubeAPIService = YouTubeAPIService(context)
    private val suggestionAPIService: SuggestionAPIService = RetrofitManager.suggestionAPIService

    suspend fun getSuggestions(query: String): List<String> {
        val response = suggestionAPIService.suggest(query)
        return if (response.isSuccessful) {
            response.body()?.suggestions ?: emptyList()
        } else {
            emptyList()
        }
    }

    suspend fun search(query: String, pageToken: String? = null): SearchListResponse = youTubeAPIService.search(query, pageToken)

    suspend fun getPopularMusic(pageToken: String?): VideoListResponse = youTubeAPIService.popularMusic(pageToken)

    companion object {
        @Volatile
        private var INSTANCE: YouTubeRepository? = null

        @JvmStatic
        fun getInstance(context: Context): YouTubeRepository {
            if (INSTANCE == null) {
                synchronized(YouTubeRepository::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = YouTubeRepository(context)
                    }
                }
            }
            return INSTANCE!!
        }
    }
}