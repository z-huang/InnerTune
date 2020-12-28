package com.zionhuang.music.youtube.extractors

import android.util.Log
import com.zionhuang.music.extensions.*
import com.zionhuang.music.youtube.models.YouTubeSearch
import com.zionhuang.music.youtube.models.YouTubeSearchItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext

object YouTubeSearchExtractor {
    private const val TAG = "YouTubeSearchExtractor"

    private const val SEARCH_URL = "https://www.youtube.com/youtubei/v1/search?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"

    suspend fun search(query: String, pageToken: String? = null): YouTubeSearch = withContext(Default) {
        val data = jsonObjectOf(
                "context" to jsonObjectOf(
                        "client" to jsonObjectOf(
                                "clientName" to "WEB",
                                "clientVersion" to "2.20201021.03.00"
                        )
                ),
                "query" to query,
                "continuation" to pageToken
        )
        Log.d(TAG, "Downloading API page")
        val response = urlRequest(SEARCH_URL, mapOf("content-type" to "application/json"), data).get()
        val res = response.parseJsonString()
        val slrContents = (res["contents"]["twoColumnSearchResultsRenderer"]["primaryContents"]["sectionListRenderer"]["contents"]
                ?: res["onResponseReceivedCommands"][0]["appendContinuationItemsAction"]["continuationItems"]).asJsonArrayOrNull
        val isrContents = slrContents[0]["itemSectionRenderer"]["contents"].asJsonArrayOrNull
                ?: return@withContext YouTubeSearch.Error("Failed to extract isr contents")

        val items = mutableListOf<YouTubeSearchItem>()
        for (content in isrContents) {
            val video = content["videoRenderer"].asJsonObjectOrNull ?: continue
            items += YouTubeSearchItem(
                    video["videoId"].asStringOrNull ?: continue,
                    video["title"]["runs"][0]["text"].asStringOrNull,
                    video["ownerText"]["runs"][0]["text"].asStringOrNull,
                    video["lengthText"]["simpleText"].asStringOrNull
            )
        }
        val nextPageToken = slrContents[1]["continuationItemRenderer"]["continuationEndpoint"]["continuationCommand"]["token"].asStringOrNull
        return@withContext YouTubeSearch.Success(query, items, nextPageToken)
    }
}