package com.zionhuang.music.utils

import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.zionhuang.music.youtube.api.YouTubeSuggestionAPIService
import com.zionhuang.music.youtube.models.YouTubeSuggestion
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitManager {
    private const val GoogleSuggestionAPIBaseUrl = "https://clients1.google.com/"

    private val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(GoogleSuggestionAPIBaseUrl)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder()
                    .registerTypeAdapter(YouTubeSuggestion::class.java, YouTubeSuggestion.deserializer)
                    .create()))
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()

    val suggestionAPIService: YouTubeSuggestionAPIService = retrofit.create(YouTubeSuggestionAPIService::class.java)
}