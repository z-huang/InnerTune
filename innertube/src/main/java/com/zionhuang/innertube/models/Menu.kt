package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class Menu(
    val menuRenderer: MenuRenderer,
) {
    private fun findEndpointByIcon(iconType: String): NavigationEndpoint? =
        menuRenderer.items.find {
            it.menuNavigationItemRenderer?.icon?.iconType == iconType || it.menuServiceItemRenderer?.icon?.iconType == iconType
        }?.let {
            it.menuNavigationItemRenderer?.navigationEndpoint ?: it.menuServiceItemRenderer?.serviceEndpoint
        } ?: menuRenderer.topLevelButtons?.find {
            it.buttonRenderer?.icon?.iconType == iconType
        }?.buttonRenderer?.navigationEndpoint

    fun toItemMenu() = ItemMenu(
        playEndpoint = findEndpointByIcon(ICON_PLAY_ARROW),
        shuffleEndpoint = findEndpointByIcon(ICON_SHUFFLE),
        radioEndpoint = findEndpointByIcon(ICON_MIX),
        playNextEndpoint = findEndpointByIcon(ICON_PLAY_NEXT),
        addToQueueEndpoint = findEndpointByIcon(ICON_ADD_TO_QUEUE),
        artistEndpoint = findEndpointByIcon(ICON_ARTIST),
        albumEndpoint = findEndpointByIcon(ICON_ALBUM),
        shareEndpoint = findEndpointByIcon(ICON_SHARE)
    )

    @Serializable
    data class MenuRenderer(
        val items: List<Item>,
        val topLevelButtons: List<TopLevelButton>?,
    ) {
        @Serializable
        data class Item(
            val menuNavigationItemRenderer: MenuNavigationItemRenderer?,
            val menuServiceItemRenderer: MenuServiceItemRenderer?,
        ) {
            @Serializable
            data class MenuNavigationItemRenderer(
                val text: Runs,
                val icon: Icon,
                val navigationEndpoint: NavigationEndpoint,
            )

            @Serializable
            data class MenuServiceItemRenderer(
                val text: Runs,
                val icon: Icon,
                val serviceEndpoint: NavigationEndpoint,
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
        const val ICON_PLAY_NEXT = "QUEUE_PLAY_NEXT"
        const val ICON_ADD_TO_QUEUE = "ADD_TO_REMOTE_QUEUE"
        const val ICON_ALBUM = "ALBUM"
        const val ICON_ARTIST = "ARTIST"
        const val ICON_SHARE = "SHARE"
    }
}
