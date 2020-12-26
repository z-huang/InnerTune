package com.zionhuang.music.extractor.models

import com.zionhuang.music.models.SearchItem

sealed class YouTubeSearch {
    class Success(val query: String, val items: List<SearchItem>, val nextPageToken: String?) : YouTubeSearch()
    class Error(val errorMessage: String) : YouTubeSearch()
}
