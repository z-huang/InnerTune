package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoints.BrowseEndpoint
import com.zionhuang.innertube.models.endpoints.NavigationEndpoint
import kotlinx.serialization.Serializable

@Serializable
data class Menu(
    val menuRenderer: MenuRenderer,
) {
    fun getShuffleEndpoint(): NavigationEndpoint? = findEndpointByIcon(ICON_SHUFFLE)
    fun getRadioEndpoint(): NavigationEndpoint? = findEndpointByIcon(ICON_MIX)
    fun getAlbumEndpoint(): BrowseEndpoint? = findEndpointByIcon(ICON_ALBUM)?.browseEndpoint
    fun getArtistEndpoint(): BrowseEndpoint? = findEndpointByIcon(ICON_ARTIST)?.browseEndpoint

    private fun findEndpointByIcon(iconType: String): NavigationEndpoint? = menuRenderer.items.find {
        it.menuNavigationItemRenderer?.icon?.iconType == iconType
    }?.menuNavigationItemRenderer?.navigationEndpoint

    @Serializable
    data class MenuRenderer(
        val items: List<Item>,
    ) {
        @Serializable
        data class Item(
            val menuNavigationItemRenderer: MenuNavigationItemRenderer?,
        ) {
            @Serializable
            data class MenuNavigationItemRenderer(
                val text: Runs,
                val icon: Icon,
                val navigationEndpoint: NavigationEndpoint,
            )
        }
    }

    companion object {
        const val ICON_SHUFFLE = "MUSIC_SHUFFLE"
        const val ICON_MIX = "MIX"
        const val ICON_ALBUM = "ALBUM"
        const val ICON_ARTIST = "ARTIST"
    }
}
