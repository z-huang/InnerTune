package com.zionhuang.innertube.models

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
        id = videoId,
        title = title.toString(),
        subtitle = longBylineText.toString(),
        thumbnails = thumbnail.thumbnails,
        menu = menu.toItemMenu(),
        navigationEndpoint = navigationEndpoint
    )
}