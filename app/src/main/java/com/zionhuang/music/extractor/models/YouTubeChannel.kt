package com.zionhuang.music.extractor.models

sealed class YouTubeChannel {
    class Success(
            val id: String,
            val name: String?,
            val avatarUrl: String?,
            val bannerUrl: String?,
    ) : YouTubeChannel()

    class Error(errorMessage: String) : YouTubeChannel()
}