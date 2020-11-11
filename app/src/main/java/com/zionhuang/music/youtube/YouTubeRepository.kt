package com.zionhuang.music.youtube

import android.content.Context
import com.google.api.services.youtube.model.SearchListResponse
import com.google.api.services.youtube.model.VideoListResponse
import io.reactivex.rxjava3.core.Observable

class YouTubeRepository private constructor(context: Context) {
    companion object {
        private const val TAG = "YoutubeRepository"

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

    private val youTubeAPIService: YouTubeAPIService = YouTubeAPIService(context)
    private val suggestionAPIService: SuggestionAPIService = RetrofitManager.getInstance().suggestionAPIService

    fun getSuggestions(query: String?): Observable<List<String>> {
        return suggestionAPIService.suggest(query)
                .map { obj: SuggestionResult -> obj.suggestions }
    }

    suspend fun search(query: String, pageToken: String? = null): SearchListResponse = youTubeAPIService.search(query, pageToken)

    suspend fun getPopularMusic(pageToken: String?): VideoListResponse = youTubeAPIService.popularMusic(pageToken)
}