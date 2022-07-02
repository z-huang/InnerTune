@file:OptIn(ExperimentalSerializationApi::class)

package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoint.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_ALBUM
import com.zionhuang.innertube.models.endpoint.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_ARTIST
import com.zionhuang.innertube.models.endpoint.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_PLAYLIST
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class MusicResponsiveListItemRenderer(
    val fixedColumns: List<FlexColumn>?,
    val flexColumns: List<FlexColumn>,
    val thumbnail: ThumbnailRenderer?,
    val menu: Menu,
    val playlistItemData: PlaylistItemData?,
    val index: Runs?,
    val navigationEndpoint: NavigationEndpoint?,
) {
    fun getTitle() = flexColumns[0].musicResponsiveListItemFlexColumnRenderer.text.toString()
    fun getSubtitle() = (flexColumns.drop(1) + fixedColumns.orEmpty())
        .filter {
            it.musicResponsiveListItemFlexColumnRenderer.text.runs.isNotEmpty()
        }.joinToString(separator = " • ") {
            it.musicResponsiveListItemFlexColumnRenderer.text.toString()
        }

    private val isSong: Boolean
        get() = navigationEndpoint == null && thumbnail!!.isSquare
    private val isVideo: Boolean
        get() = navigationEndpoint == null && !thumbnail!!.isSquare
    private val isPlaylist: Boolean
        get() = navigationEndpoint?.browseEndpoint?.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType == MUSIC_PAGE_TYPE_PLAYLIST
    private val isAlbum: Boolean
        get() = navigationEndpoint?.browseEndpoint?.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType == MUSIC_PAGE_TYPE_ALBUM
    private val isArtist: Boolean
        get() = navigationEndpoint?.browseEndpoint?.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType == MUSIC_PAGE_TYPE_ARTIST

    fun toItem(): Item = when {
        isSong -> SongItem.from(this)
        isVideo -> VideoItem.from(this)
        isPlaylist -> PlaylistItem.from(this)
        isAlbum -> AlbumItem.from(this)
        isArtist -> ArtistItem.from(this)
        else -> throw UnsupportedOperationException("Unknown item type")
    }

    @Serializable
    data class FlexColumn(
        @JsonNames("musicResponsiveListItemFixedColumnRenderer")
        val musicResponsiveListItemFlexColumnRenderer: MusicResponsiveListItemFlexColumnRenderer,
    ) {
        @Serializable
        data class MusicResponsiveListItemFlexColumnRenderer(
            val text: Runs,
        )
    }

    @Serializable
    data class PlaylistItemData(
        val playlistSetVideoId: String?,
        val videoId: String,
    )
}
