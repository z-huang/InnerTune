package com.zionhuang.music.youtube.models

sealed class YouTubeSearch {
    class Success(val query: String, val items: List<YouTubeSearchItem>, val nextPageToken: String?) : YouTubeSearch()
    class Error(val errorMessage: String) : YouTubeSearch()
}
