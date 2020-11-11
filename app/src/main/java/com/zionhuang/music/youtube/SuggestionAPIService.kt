package com.zionhuang.music.youtube

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SuggestionAPIService {
    @GET("/complete/search?client=firefox&ds=yt")
    suspend fun suggest(@Query("q") query: String): Response<SuggestionResult>
}