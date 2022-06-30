package com.zionhuang.innertube.models.endpoints

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
}
