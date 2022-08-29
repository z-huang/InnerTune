package com.zionhuang.innertube.models

import android.os.Parcelable
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_ALBUM
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_ARTIST
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_PLAYLIST
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_USER_CHANNEL
import com.zionhuang.innertube.models.WatchEndpoint.WatchEndpointMusicSupportedConfigs.WatchEndpointMusicConfig.Companion.MUSIC_VIDEO_TYPE_ATV
import com.zionhuang.innertube.models.WatchEndpoint.WatchEndpointMusicSupportedConfigs.WatchEndpointMusicConfig.Companion.MUSIC_VIDEO_TYPE_OMV
import com.zionhuang.innertube.models.WatchEndpoint.WatchEndpointMusicSupportedConfigs.WatchEndpointMusicConfig.Companion.MUSIC_VIDEO_TYPE_UGC
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class NavigationEndpoint(
    val watchEndpoint: WatchEndpoint? = null,
    val watchPlaylistEndpoint: WatchPlaylistEndpoint? = null,
    val browseEndpoint: BrowseEndpoint? = null,
    val searchEndpoint: SearchEndpoint? = null,
    val queueAddEndpoint: QueueAddEndpoint? = null,
    val shareEntityEndpoint: ShareEntityEndpoint? = null,
    // Custom endpoint
    val browseLocalArtistSongsEndpoint: BrowseLocalArtistSongsEndpoint? = null,
) : Parcelable {
    val endpoint: Endpoint?
        get() = watchEndpoint
            ?: watchPlaylistEndpoint
            ?: browseEndpoint
            ?: searchEndpoint
            ?: queueAddEndpoint
            ?: shareEntityEndpoint
            ?: browseLocalArtistSongsEndpoint

    fun getEndpointType(): Int = when (val ep = endpoint) {
        is WatchEndpoint -> when (ep.watchEndpointMusicSupportedConfigs?.watchEndpointMusicConfig?.musicVideoType) {
            MUSIC_VIDEO_TYPE_ATV -> ITEM_SONG
            MUSIC_VIDEO_TYPE_OMV, MUSIC_VIDEO_TYPE_UGC -> ITEM_VIDEO
            else -> ITEM_UNKNOWN
        }
        is BrowseEndpoint -> when (ep.browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType) {
            MUSIC_PAGE_TYPE_ALBUM -> ITEM_ALBUM
            MUSIC_PAGE_TYPE_PLAYLIST -> ITEM_PLAYLIST
            MUSIC_PAGE_TYPE_ARTIST -> ITEM_ARTIST
            MUSIC_PAGE_TYPE_USER_CHANNEL -> ITEM_ARTIST
            else -> ITEM_UNKNOWN
        }
        else -> ITEM_UNKNOWN
    }
}
