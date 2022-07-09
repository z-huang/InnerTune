package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_ALBUM
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_ARTIST
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_PLAYLIST
import kotlinx.serialization.Serializable

@Serializable
data class MusicTwoRowItemRenderer(
    val title: Runs,
    val subtitle: Runs,
    val menu: Menu,
    val thumbnailRenderer: ThumbnailRenderer,
    val navigationEndpoint: NavigationEndpoint,
    // val thumbnailOverlay: ThumbnailOverlay, (for playing the album directly)
) {
    private val isSong: Boolean
        get() = navigationEndpoint.endpoint is WatchEndpoint && thumbnailRenderer.isSquare
    private val isVideo: Boolean
        get() = navigationEndpoint.endpoint is WatchEndpoint && !thumbnailRenderer.isSquare
    private val isPlaylist: Boolean
        get() = navigationEndpoint.browseEndpoint?.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType == MUSIC_PAGE_TYPE_PLAYLIST
    private val isAlbum: Boolean
        get() = navigationEndpoint.browseEndpoint?.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType == MUSIC_PAGE_TYPE_ALBUM
    private val isArtist: Boolean
        get() = navigationEndpoint.browseEndpoint?.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType == MUSIC_PAGE_TYPE_ARTIST

    fun toItem(): Item = when {
        isSong -> SongItem.from(this)
        isVideo -> VideoItem.from(this)
        isPlaylist -> PlaylistItem.from(this)
        isAlbum -> AlbumItem.from(this)
        isArtist -> ArtistItem.from(this)
        else -> throw UnsupportedOperationException("Unknown item type")
    }
}