package com.zionhuang.music.youtube.models

import com.zionhuang.music.youtube.extractors.YouTubeStreamExtractor

sealed class YouTubeStream {
    data class Success(
            val id: String,
            val title: String,
            val channelId: String,
            val channelTitle: String,
            val duration: Int,
            val thumbnailUrl: String?,
            val formats: List<YtFormat>,
    ) : YouTubeStream()

    class Error(val errorCode: YouTubeStreamExtractor.ErrorCode, val errorMessage: String) : YouTubeStream()
}