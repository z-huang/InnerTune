package com.zionhuang.innertube.models.endpoint

import kotlinx.serialization.Serializable

@Serializable
sealed class Endpoint

@Serializable
data class WatchEndpoint(
    val videoId: String,
    val playlistId: String?,
    val playlistSetVideoId: String?,
    val params: String?,
    val index: Int?,
    val watchEndpointMusicSupportedConfigs: WatchEndpointMusicSupportedConfigs? = null,
) : Endpoint(), java.io.Serializable {
    @Serializable
    data class WatchEndpointMusicSupportedConfigs(
        val watchEndpointMusicConfig: WatchEndpointMusicConfig,
    ) {
        @Serializable
        data class WatchEndpointMusicConfig(
            val musicVideoType: String,
        )
    }
}

@Serializable
data class BrowseEndpoint(
    val browseId: String,
    val params: String? = null,
    val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs? = null,
) : Endpoint(), java.io.Serializable {
    @Serializable
    data class BrowseEndpointContextSupportedConfigs(
        val browseEndpointContextMusicConfig: BrowseEndpointContextMusicConfig,
    ) {
        @Serializable
        data class BrowseEndpointContextMusicConfig(
            val pageType: String,
        ) {
            companion object {
                const val MUSIC_PAGE_TYPE_ALBUM = "MUSIC_PAGE_TYPE_ALBUM"
                const val MUSIC_PAGE_TYPE_PLAYLIST = "MUSIC_PAGE_TYPE_PLAYLIST"
                const val MUSIC_PAGE_TYPE_ARTIST = "MUSIC_PAGE_TYPE_ARTIST"
            }
        }
    }
}

@Serializable
data class WatchPlaylistEndpoint(
    val params: String?,
    val playlistId: String,
) : Endpoint(), java.io.Serializable

@Serializable
data class SearchEndpoint(
    val params: String?,
    val query: String,
) : Endpoint(), java.io.Serializable

@Serializable
data class ShareEntityEndpoint(
    val serializedShareEntity: String,
) : Endpoint(), java.io.Serializable