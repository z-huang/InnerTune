package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class SearchSuggestionsSectionRenderer(
    val contents: List<Content>,
) {
    @Serializable
    data class Content(
        val searchSuggestionRenderer: SearchSuggestionRenderer?,
        val musicTwoColumnItemRenderer: MusicTwoColumnItemRenderer?,
    ) {
        fun toItem(): BaseItem? = when {
            searchSuggestionRenderer != null -> SuggestionTextItem(searchSuggestionRenderer.suggestion.toString())
            musicTwoColumnItemRenderer != null -> when (musicTwoColumnItemRenderer.navigationEndpoint.getItemType()) {
                ITEM_SONG -> SongItem(
                    title = musicTwoColumnItemRenderer.title.toString(),
                    subtitle = musicTwoColumnItemRenderer.subtitle.toString(),
                    thumbnails = musicTwoColumnItemRenderer.thumbnail.getThumbnails(),
                    menu = musicTwoColumnItemRenderer.menu.toItemMenu(),
                    navigationEndpoint = musicTwoColumnItemRenderer.navigationEndpoint
                )
                ITEM_VIDEO -> VideoItem(
                    title = musicTwoColumnItemRenderer.title.toString(),
                    subtitle = musicTwoColumnItemRenderer.subtitle.toString(),
                    thumbnails = musicTwoColumnItemRenderer.thumbnail.getThumbnails(),
                    menu = musicTwoColumnItemRenderer.menu.toItemMenu(),
                    navigationEndpoint = musicTwoColumnItemRenderer.navigationEndpoint
                )
                ITEM_ALBUM -> AlbumItem(
                    title = musicTwoColumnItemRenderer.title.toString(),
                    subtitle = musicTwoColumnItemRenderer.subtitle.toString(),
                    thumbnails = musicTwoColumnItemRenderer.thumbnail.getThumbnails(),
                    menu = musicTwoColumnItemRenderer.menu.toItemMenu(),
                    navigationEndpoint = musicTwoColumnItemRenderer.navigationEndpoint
                )
                ITEM_PLAYLIST -> PlaylistItem(
                    title = musicTwoColumnItemRenderer.title.toString(),
                    subtitle = musicTwoColumnItemRenderer.subtitle.toString(),
                    thumbnails = musicTwoColumnItemRenderer.thumbnail.getThumbnails(),
                    menu = musicTwoColumnItemRenderer.menu.toItemMenu(),
                    navigationEndpoint = musicTwoColumnItemRenderer.navigationEndpoint
                )
                ITEM_ARTIST -> ArtistItem(
                    title = musicTwoColumnItemRenderer.title.toString(),
                    subtitle = musicTwoColumnItemRenderer.subtitle.toString(),
                    thumbnails = musicTwoColumnItemRenderer.thumbnail.getThumbnails(),
                    menu = musicTwoColumnItemRenderer.menu.toItemMenu(),
                    navigationEndpoint = musicTwoColumnItemRenderer.navigationEndpoint
                )
                else -> null
            }
            else -> null
        }

        @Serializable
        data class SearchSuggestionRenderer(
            val suggestion: Runs,
            val navigationEndpoint: NavigationEndpoint,
        )

        @Serializable
        data class MusicTwoColumnItemRenderer(
            val title: Runs,
            val subtitle: Runs,
            val thumbnail: ThumbnailRenderer,
            val menu: Menu,
            val navigationEndpoint: NavigationEndpoint,
        )
    }
}
