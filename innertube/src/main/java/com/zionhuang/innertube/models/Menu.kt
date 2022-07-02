package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class Menu(
    val menuRenderer: MenuRenderer,
) {
    private fun findEndpointByIcon(iconType: String): NavigationEndpoint? = menuRenderer.items.find {
        it.menuNavigationItemRenderer?.icon?.iconType == iconType
    }?.menuNavigationItemRenderer?.navigationEndpoint ?: menuRenderer.topLevelButtons?.find {
        it.buttonRenderer?.icon?.iconType == iconType
    }?.buttonRenderer?.navigationEndpoint

    fun toItemMenu() = ItemMenu(
        playEndpoint = findEndpointByIcon(ICON_PLAY_ARROW),
        shuffleEndpoint = findEndpointByIcon(ICON_SHUFFLE),
        radioEndpoint = findEndpointByIcon(ICON_MIX),
        artistEndpoint = findEndpointByIcon(ICON_ARTIST)?.browseEndpoint,
        albumEndpoint = findEndpointByIcon(ICON_ALBUM)?.browseEndpoint,
        shareEndpoint = findEndpointByIcon(ICON_SHARE)?.shareEntityEndpoint
    )

    @Serializable
    data class MenuRenderer(
        val items: List<Item>,
        val topLevelButtons: List<TopLevelButton>?,
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

        @Serializable
        data class TopLevelButton(
            val buttonRenderer: ButtonRenderer?,
        ) {
            @Serializable
            data class ButtonRenderer(
                val icon: Icon,
                val navigationEndpoint: NavigationEndpoint,
            )
        }
    }

    companion object {
        const val ICON_PLAY_ARROW = "PLAY_ARROW"
        const val ICON_SHUFFLE = "MUSIC_SHUFFLE"
        const val ICON_MIX = "MIX"
        const val ICON_ALBUM = "ALBUM"
        const val ICON_ARTIST = "ARTIST"
        const val ICON_SHARE = "SHARE"
    }
}
