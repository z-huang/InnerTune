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
                    id = musicTwoColumnItemRenderer.navigationEndpoint.watchEndpoint!!.videoId,
                    title = musicTwoColumnItemRenderer.title.toString(),
                    subtitle = musicTwoColumnItemRenderer.subtitle.toString(),
                    thumbnails = musicTwoColumnItemRenderer.thumbnail.getThumbnails(),
                    menu = musicTwoColumnItemRenderer.menu.toItemMenu(),
                    navigationEndpoint = musicTwoColumnItemRenderer.navigationEndpoint
                )
                ITEM_VIDEO -> VideoItem(
                    id = musicTwoColumnItemRenderer.navigationEndpoint.watchEndpoint!!.videoId,
                    title = musicTwoColumnItemRenderer.title.toString(),
                    subtitle = musicTwoColumnItemRenderer.subtitle.toString(),
                    thumbnails = musicTwoColumnItemRenderer.thumbnail.getThumbnails(),
                    menu = musicTwoColumnItemRenderer.menu.toItemMenu(),
                    navigationEndpoint = musicTwoColumnItemRenderer.navigationEndpoint
                )
                ITEM_ALBUM -> {
                    val menu = musicTwoColumnItemRenderer.menu.toItemMenu()
                    AlbumItem(
                        id = menu.shuffleEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")
                            ?: menu.radioEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")!!,
                        title = musicTwoColumnItemRenderer.title.toString(),
                        subtitle = musicTwoColumnItemRenderer.subtitle.toString(),
                        thumbnails = musicTwoColumnItemRenderer.thumbnail.getThumbnails(),
                        menu = menu,
                        navigationEndpoint = musicTwoColumnItemRenderer.navigationEndpoint
                    )
                }
                ITEM_PLAYLIST -> {
                    val menu = musicTwoColumnItemRenderer.menu.toItemMenu()
                    PlaylistItem(
                        id = menu.shuffleEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")
                            ?: menu.radioEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")!!,
                        title = musicTwoColumnItemRenderer.title.toString(),
                        subtitle = musicTwoColumnItemRenderer.subtitle.toString(),
                        thumbnails = musicTwoColumnItemRenderer.thumbnail.getThumbnails(),
                        menu = menu,
                        navigationEndpoint = musicTwoColumnItemRenderer.navigationEndpoint
                    )
                }
                ITEM_ARTIST -> ArtistItem(
                    id = musicTwoColumnItemRenderer.navigationEndpoint.browseEndpoint!!.browseId,
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
