package com.zionhuang.music.youtube

import android.content.Context
import com.zionhuang.music.youtube.api.YouTubeAPIService
import com.zionhuang.music.youtube.extractors.YouTubeChannelExtractor
import com.zionhuang.music.youtube.extractors.YouTubeSearchExtractor
import com.zionhuang.music.youtube.extractors.YouTubeStreamExtractor
import com.zionhuang.music.youtube.models.YouTubeChannel
import com.zionhuang.music.youtube.models.YouTubeSearch
import com.zionhuang.music.youtube.models.YouTubeSearchItem

class YouTubeExtractor private constructor(private val context: Context) {
    private val youTubeStreamExtractor by lazy { YouTubeStreamExtractor.getInstance(context) }

    suspend fun search(query: String, pageToken: String? = null): YouTubeSearch = YouTubeSearchExtractor.search(query, pageToken)

    suspend fun search(query: String, pageToken: String? = null, key: String): YouTubeSearch {
        val res = YouTubeAPIService(context).search(query, pageToken)
        val items = res.items.filter { it.id.kind == "youtube#video" }.map {
            YouTubeSearchItem(
                    id = it.id.videoId,
                    title = it.snippet.title,
                    channelTitle = it.snippet.channelTitle
            )
        }
        return YouTubeSearch.Success(query, items, res.nextPageToken)
    }

    suspend fun getChannel(channelId: String): YouTubeChannel =
            YouTubeChannelExtractor.extractChannel(channelId)

    suspend fun extractStream(videoId: String) = youTubeStreamExtractor.extract(videoId)

    fun extractId(url: String) = YouTubeStreamExtractor.extractId(url)

    companion object {
        @Volatile
        private var INSTANCE: YouTubeExtractor? = null

        @JvmStatic
        fun getInstance(context: Context): YouTubeExtractor {
            if (INSTANCE == null) {
                synchronized(YouTubeExtractor::class) {
                    if (INSTANCE == null) {
                        INSTANCE = YouTubeExtractor(context)
                    }
                }
            }
            return INSTANCE!!
        }

        private const val TAG = "YouTubeExtractor"
    }
}
