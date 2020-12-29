package com.zionhuang.music.youtube.models

sealed class YouTubeChannel {
    data class Success(
            val id: String,
            val name: String?,
            val avatarUrl: String?,
            val bannerUrl: String?,
    ) : YouTubeChannel()

    class Error(val errorMessage: String?) : YouTubeChannel()
}