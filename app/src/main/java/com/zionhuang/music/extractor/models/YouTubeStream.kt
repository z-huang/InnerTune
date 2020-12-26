package com.zionhuang.music.extractor.models

import com.zionhuang.music.extractor.ytextractors.YouTubeStreamExtractor

sealed class YouTubeStream {
    class Success(
            val id: String,
            val title: String,
            val channelId: String,
            val channelTitle: String,
            val duration: Int,
            val formats: List<YtFormat>,
    ) : YouTubeStream()

    class Error(val errorCode: YouTubeStreamExtractor.ErrorCode, val errorMessage: String) : YouTubeStream()
}