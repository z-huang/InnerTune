package com.zionhuang.innertube.models

data class NextResult(
    val items: List<SongItem>,
    val currentIndex: Int? = null,
    val lyricsEndpoint: BrowseEndpoint? = null,
    val continuation: String?,
)
