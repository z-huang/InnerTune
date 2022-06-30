package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoint.BrowseEndpoint
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

    fun toItemMenu() = Item.Menu(
        shuffleEndpoint = findEndpointByIcon(ICON_SHUFFLE),
        radioEndpoint = findEndpointByIcon(ICON_MIX),
        artistEndpoint = findEndpointByIcon(ICON_ARTIST)?.browseEndpoint,
        albumEndpoint = findEndpointByIcon(ICON_ALBUM)?.browseEndpoint,
        shareEndpoint = findEndpointByIcon(ICON_SHARE)?.shareEntityEndpoint
    )

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
        const val ICON_SHARE = "SHARE"
    }
}
