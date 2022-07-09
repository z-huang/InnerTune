package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_ALBUM
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_ARTIST
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_PLAYLIST
import com.zionhuang.innertube.models.WatchEndpoint.WatchEndpointMusicSupportedConfigs.WatchEndpointMusicConfig.Companion.MUSIC_VIDEO_TYPE_ATV
import com.zionhuang.innertube.models.WatchEndpoint.WatchEndpointMusicSupportedConfigs.WatchEndpointMusicConfig.Companion.MUSIC_VIDEO_TYPE_OMV
import com.zionhuang.innertube.models.WatchEndpoint.WatchEndpointMusicSupportedConfigs.WatchEndpointMusicConfig.Companion.MUSIC_VIDEO_TYPE_UGC
import kotlinx.serialization.Serializable

@Serializable
data class NavigationEndpoint(
    val watchEndpoint: WatchEndpoint?,
    val browseEndpoint: BrowseEndpoint?,
    val watchPlaylistEndpoint: WatchPlaylistEndpoint?,
    val searchEndpoint: SearchEndpoint?,
    val shareEntityEndpoint: ShareEntityEndpoint?,
) {
    val endpoint: Endpoint?
        get() = watchEndpoint ?: browseEndpoint ?: watchPlaylistEndpoint ?: searchEndpoint ?: shareEntityEndpoint

    fun getItemType(): Int = when (val ep = endpoint) {
        is WatchEndpoint -> when (ep.watchEndpointMusicSupportedConfigs?.watchEndpointMusicConfig?.musicVideoType) {
            MUSIC_VIDEO_TYPE_ATV -> ITEM_SONG
            MUSIC_VIDEO_TYPE_OMV, MUSIC_VIDEO_TYPE_UGC -> ITEM_VIDEO
            else -> ITEM_UNKNOWN
        }
        is BrowseEndpoint -> when (ep.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType) {
            MUSIC_PAGE_TYPE_ALBUM -> ITEM_ALBUM
            MUSIC_PAGE_TYPE_PLAYLIST -> ITEM_PLAYLIST
            MUSIC_PAGE_TYPE_ARTIST -> ITEM_ARTIST
            else -> ITEM_UNKNOWN
        }
        else -> ITEM_UNKNOWN
    }

}
