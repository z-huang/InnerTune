package com.zionhuang.music.youtube.models

data class YouTubeSearchItem(
        val id: String,
        val title: String?,
        val channelTitle: String?,
        val duration: String? = null,
)
