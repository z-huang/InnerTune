package com.zionhuang.innertube.pages

import com.zionhuang.innertube.models.SongItem

data class PlaylistContinuationPage(
    val songs: List<SongItem>,
    val continuation: String?,
)
