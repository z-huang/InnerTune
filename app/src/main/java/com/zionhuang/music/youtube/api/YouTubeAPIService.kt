package com.zionhuang.music.youtube.api

import android.content.Context
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.youtube.YouTube
import com.google.api.services.youtube.model.SearchListResponse
import com.google.api.services.youtube.model.VideoListResponse
import com.zionhuang.music.R
import com.zionhuang.music.extensions.preference
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class YouTubeAPIService(context: Context) {
    companion object {
        private const val TAG = "YoutubeAPIService"
        private const val REGION_CODE = "TW"
        private const val CATEGORY_MUSIC = "10"
    }

    private val apiKey by context.preference(R.string.pref_api_key, "")
    private val youTube: YouTube = YouTube.Builder(NetHttpTransport.Builder().build(), GsonFactory(), null)
            .setApplicationName(context.resources.getString(R.string.app_name))
            .build()

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun search(query: String, pageToken: String?): SearchListResponse = withContext(IO) {
        youTube.search().list(listOf("snippet"))
                .setKey(apiKey)
                .setQ(query)
                .setPageToken(pageToken)
                .setMaxResults(20L)
                .execute()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun popularMusic(pageToken: String?): VideoListResponse = withContext(IO) {
        youTube.videos().list(listOf("snippet,contentDetails,statistics"))
                .setKey(apiKey)
                .setChart("mostPopular")
                .setVideoCategoryId(CATEGORY_MUSIC)
                .setRegionCode(REGION_CODE)
                .setPageToken(pageToken)
                .setMaxResults(20L)
                .execute()
    }
}