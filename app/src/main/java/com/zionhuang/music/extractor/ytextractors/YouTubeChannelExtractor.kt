package com.zionhuang.music.extractor.ytextractors

import com.zionhuang.music.extensions.*
import com.zionhuang.music.extractor.models.YouTubeChannel
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.withContext

class YouTubeChannelExtractor() {
    companion object {
        suspend fun extractChannel(id: String): YouTubeChannel = withContext(Default) {
            val url = "https://www.youtube.com/channel/$id/videos?pbj=1&view=0&flow=grid"
            val response = urlRequest(url, mapOf(
                    "X-YouTube-Client-Name" to "1",
                    "X-YouTube-Client-Version" to "2.20201021.03.00"
            )).get()
            val initialData = response.parseJsonString().asJsonObjectOrNull!!
            YouTubeChannel.Success(
                    id = id,
                    name = initialData["header"]["c4TabbedHeaderRenderer"]["title"].asStringOrNull,
                    avatarUrl = initialData["header"]["c4TabbedHeaderRenderer"]["avatar"]["thumbnails"][0]["url"].asStringOrNull,
                    bannerUrl = initialData["header"]["c4TabbedHeaderRenderer"]["avatar"]["banner"]["thumbnails"][0]["url"].asStringOrNull
            )
        }
    }
}