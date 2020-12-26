@file:Suppress("RegExpRedundantEscape", "SpellCheckingInspection")

package com.zionhuang.music.extractor

import android.content.Context
import com.zionhuang.music.extractor.ytextractors.YouTubeSearchExtractor
import com.zionhuang.music.extractor.ytextractors.YouTubeStreamExtractor
import com.zionhuang.music.extractor.models.YouTubeSearch

typealias SignatureFunctionKt = (String) -> String

class YouTubeExtractor private constructor(private val context: Context) {
    suspend fun search(query: String, pageToken: String? = null): YouTubeSearch = YouTubeSearchExtractor.search(query, pageToken)

    suspend fun extract(videoId: String) = YouTubeStreamExtractor.getInstance(context).extract(videoId)

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
