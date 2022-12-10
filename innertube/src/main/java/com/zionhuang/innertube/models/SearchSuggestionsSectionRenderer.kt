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
        ) {
            fun toItem(): YTItem? = when (navigationEndpoint.getEndpointType()) {
                ITEM_SONG -> {
                    val menu = menu.toItemMenu()
                    SongItem(
                        id = navigationEndpoint.watchEndpoint?.videoId!!,
                        title = title.toString(),
                        subtitle = subtitle.toString(),
                        artists = listOf(Run(
                            text = subtitle.runs.last().text,
                            navigationEndpoint = menu.artistEndpoint
                        )),
                        thumbnails = thumbnail.getThumbnails(),
                        menu = menu,
                        navigationEndpoint = navigationEndpoint
                    )
                }
                ITEM_VIDEO -> SongItem(
                    id = navigationEndpoint.watchEndpoint?.videoId!!,
                    title = title.toString(),
                    subtitle = subtitle.toString(),
                    artists = listOf(subtitle.runs[0]),
                    thumbnails = thumbnail.getThumbnails(),
                    menu = menu.toItemMenu(),
                    navigationEndpoint = navigationEndpoint
                )
                ITEM_ALBUM -> {
                    val menu = menu.toItemMenu()
                    AlbumItem(
                        id = navigationEndpoint.browseEndpoint!!.browseId,
                        playlistId = menu.shuffleEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")
                            ?: menu.radioEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")!!,
                        title = title.toString(),
                        subtitle = subtitle.toString(),
                        thumbnails = thumbnail.getThumbnails(),
                        menu = menu,
                        navigationEndpoint = navigationEndpoint
                    )
                }
                ITEM_PLAYLIST -> {
                    val menu = menu.toItemMenu()
                    PlaylistItem(
                        id = menu.shuffleEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")
                            ?: menu.radioEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")!!,
                        title = title.toString(),
                        subtitle = subtitle.toString(),
                        thumbnails = thumbnail.getThumbnails(),
                        menu = menu,
                        navigationEndpoint = navigationEndpoint
                    )
                }
                ITEM_ARTIST -> ArtistItem(
                    id = navigationEndpoint.browseEndpoint!!.browseId,
                    title = title.toString(),
                    subtitle = subtitle.toString(),
                    thumbnails = thumbnail.getThumbnails(),
                    menu = menu.toItemMenu(),
                    navigationEndpoint = navigationEndpoint
                )
                else -> null
            }
        }
    }
}
