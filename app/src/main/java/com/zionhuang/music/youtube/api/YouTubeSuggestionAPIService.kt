package com.zionhuang.music.youtube.api

import com.zionhuang.music.youtube.models.YouTubeSuggestion
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeSuggestionAPIService {
    @GET("/complete/search?client=firefox&ds=yt")
    suspend fun suggest(@Query("q") query: String): Response<YouTubeSuggestion>
}