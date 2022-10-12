package com.zionhuang.innertube.models

import android.os.Parcelable
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_ALBUM
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_ARTIST
import com.zionhuang.innertube.models.BrowseEndpoint.BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig.Companion.MUSIC_PAGE_TYPE_PLAYLIST
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
sealed class Endpoint : Parcelable

@Parcelize
@Serializable
data class WatchEndpoint(
    val videoId: String? = null,
    val playlistId: String? = null,
    val playlistSetVideoId: String? = null,
    val params: String? = null,
    val index: Int? = null,
    val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs? = null,
) : Endpoint() {
    @Parcelize
    @Serializable
    data class WatchEndpointMusicSupportedConfigs(
        val watchEndpointMusicConfig: WatchEndpointMusicConfig,
    ) : Parcelable {
        @Parcelize
        @Serializable
        data class WatchEndpointMusicConfig(
            val musicVideoType: String,
        ) : Parcelable {
            companion object {
                const val MUSIC_VIDEO_TYPE_OMV = "MUSIC_VIDEO_TYPE_OMV"
                const val MUSIC_VIDEO_TYPE_UGC = "MUSIC_VIDEO_TYPE_UGC"
                const val MUSIC_VIDEO_TYPE_ATV = "MUSIC_VIDEO_TYPE_ATV"
            }
        }
    }
}

@Parcelize
@Serializable
data class WatchPlaylistEndpoint(
    val params: String? = null,
    val playlistId: String,
) : Endpoint() {
    fun toWatchEndpoint() = WatchEndpoint(
        videoId = null,
        playlistId = playlistId,
        playlistSetVideoId = null,
        params = params,
        index = null,
        watchEndpointMusicSupportedConfigs = null
    )
}

@Parcelize
@Serializable
data class BrowseEndpoint(
    val browseId: String,
    val params: String? = null,
    val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs? = null,
) : Endpoint() {
    val isArtistEndpoint: Boolean
        get() = browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType == MUSIC_PAGE_TYPE_ARTIST
    val isAlbumEndpoint: Boolean
        get() = browseEndpointContextSupportedConfigs?.browseEndpointContextMusicConfig?.pageType == MUSIC_PAGE_TYPE_ALBUM

    @Parcelize
    @Serializable
    data class BrowseEndpointContextSupportedConfigs(
        val browseEndpointContextMusicConfig: BrowseEndpointContextMusicConfig,
    ) : Parcelable {
        @Parcelize
        @Serializable
        data class BrowseEndpointContextMusicConfig(
            val pageType: String,
        ) : Parcelable {
            companion object {
                const val MUSIC_PAGE_TYPE_ALBUM = "MUSIC_PAGE_TYPE_ALBUM"
                const val MUSIC_PAGE_TYPE_AUDIOBOOK = "MUSIC_PAGE_TYPE_AUDIOBOOK"
                const val MUSIC_PAGE_TYPE_PLAYLIST = "MUSIC_PAGE_TYPE_PLAYLIST"
                const val MUSIC_PAGE_TYPE_ARTIST = "MUSIC_PAGE_TYPE_ARTIST"
                const val MUSIC_PAGE_TYPE_USER_CHANNEL = "MUSIC_PAGE_TYPE_USER_CHANNEL"
                const val MUSIC_PAGE_TYPE_TRACK_LYRICS = "MUSIC_PAGE_TYPE_TRACK_LYRICS"
                const val MUSIC_PAGE_TYPE_TRACK_RELATED = "MUSIC_PAGE_TYPE_TRACK_RELATED"
            }
        }
    }

    companion object {
        fun artistBrowseEndpoint(artistId: String) = BrowseEndpoint(
            browseId = artistId,
            browseEndpointContextSupportedConfigs = BrowseEndpointContextSupportedConfigs(
                browseEndpointContextMusicConfig = BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig(
                    pageType = MUSIC_PAGE_TYPE_ARTIST
                )
            )
        )

        fun albumBrowseEndpoint(albumId: String) = BrowseEndpoint(
            browseId = albumId,
            browseEndpointContextSupportedConfigs = BrowseEndpointContextSupportedConfigs(
                browseEndpointContextMusicConfig = BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig(
                    pageType = MUSIC_PAGE_TYPE_ALBUM
                )
            )
        )

        fun playlistBrowseEndpoint(playlistId: String) = BrowseEndpoint(
            browseId = playlistId,
            browseEndpointContextSupportedConfigs = BrowseEndpointContextSupportedConfigs(
                browseEndpointContextMusicConfig = BrowseEndpointContextSupportedConfigs.BrowseEndpointContextMusicConfig(
                    pageType = MUSIC_PAGE_TYPE_PLAYLIST
                )
            )
        )
    }
}

@Parcelize
@Serializable
data class SearchEndpoint(
    val params: String?,
    val query: String,
) : Endpoint()

@Parcelize
@Serializable
data class QueueAddEndpoint(
    val queueInsertPosition: String,
    val queueTarget: QueueTarget,
) : Endpoint() {
    @Parcelize
    @Serializable
    data class QueueTarget(
        val videoId: String? = null,
        val playlistId: String? = null,
    ) : Parcelable

    companion object {
        const val INSERT_AFTER_CURRENT_VIDEO = "INSERT_AFTER_CURRENT_VIDEO"
        const val INSERT_AT_END = "INSERT_AT_END"
    }
}

@Parcelize
@Serializable
data class ShareEntityEndpoint(
    val serializedShareEntity: String,
) : Endpoint()

// Custom endpoint
@Parcelize
@Serializable
data class BrowseLocalArtistSongsEndpoint(
    val artistId: String,
) : Endpoint()