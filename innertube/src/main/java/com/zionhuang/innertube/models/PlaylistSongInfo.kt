package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistSongInfo(
    val lyricsEndpoint: BrowseEndpoint,
    val relatedEndpoint: BrowseEndpoint,
)