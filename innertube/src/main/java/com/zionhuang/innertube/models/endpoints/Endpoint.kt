package com.zionhuang.innertube.models.endpoints

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
) : Endpoint() {
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
    val params: String?,
    val browseEndpointContextSupportedConfigs: BrowseEndpointContextSupportedConfigs?,
) : Endpoint() {
    @Serializable
    data class BrowseEndpointContextSupportedConfigs(
        val browseEndpointContextMusicConfig: BrowseEndpointContextMusicConfig,
    ) {
        @Serializable
        data class BrowseEndpointContextMusicConfig(
            val pageType: String,
        )
    }
}

@Serializable
data class WatchPlaylistEndpoint(
    val params: String?,
    val playlistId: String,
) : Endpoint()

@Serializable
data class SearchEndpoint(
    val params: String?,
    val query: String,
) : Endpoint()

@Serializable
data class ShareEntityEndpoint(
    val serializedShareEntity: String,
) : Endpoint()