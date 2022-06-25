package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoints.NavigationEndpoint
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistPanelVideoRenderer(
    val title: Runs,
    val lengthText: Runs,
    val longBylineText: Runs,
    val shortBylineText: Runs,
    val videoId: String,
    val playlistSetVideoId: String?,
    val selected: Boolean,
    val thumbnail: Thumbnails,
    val menu: Menu,
    val navigationEndpoint: NavigationEndpoint,
) {
    fun toSongItem(): SongItem = SongItem(
        title = title.toString(),
        subtitle = longBylineText.toString(),
        artistEndpoint = menu.getArtistEndpoint(),
        albumEndpoint = menu.getAlbumEndpoint(),
        thumbnails = thumbnail.thumbnails,
        navigationEndpoint = navigationEndpoint
    )
}