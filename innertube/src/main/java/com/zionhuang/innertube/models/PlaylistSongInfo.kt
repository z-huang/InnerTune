package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoints.BrowseEndpoint
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistSongInfo(
    val lyricsEndpoint: BrowseEndpoint,
    val relatedEndpoint: BrowseEndpoint,
)