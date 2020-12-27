package com.zionhuang.music.youtube.extractors

import com.zionhuang.music.extensions.*
import com.zionhuang.music.youtube.models.YouTubeChannel
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext

object YouTubeChannelExtractor {
    suspend fun extractChannel(id: String): YouTubeChannel = withContext(Default) {
        val url = "https://www.youtube.com/channel/$id/videos?pbj=1&view=0&flow=grid"
        val response = urlRequest(url, mapOf(
                "content-type" to "application/json",
                "X-YouTube-Client-Name" to "1",
                "X-YouTube-Client-Version" to "2.20201021.03.00"
        )).get()
        val initialData = response.parseJsonString()[1]["response"].asJsonObjectOrNull
                ?: return@withContext YouTubeChannel.Error("Failed to get initial data")
        YouTubeChannel.Success(
                id = id,
                name = initialData["header"]["c4TabbedHeaderRenderer"]["title"].asStringOrNull,
                avatarUrl = initialData["header"]["c4TabbedHeaderRenderer"]["avatar"]["thumbnails"].last()["url"].asStringOrNull,
                bannerUrl = initialData["header"]["c4TabbedHeaderRenderer"]["banner"]["thumbnails"].last()["url"].asStringOrNull
        )
    }
}