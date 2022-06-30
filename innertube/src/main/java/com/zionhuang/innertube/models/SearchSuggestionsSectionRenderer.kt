package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoint.NavigationEndpoint
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
        fun toSuggestionItem() = when {
            searchSuggestionRenderer != null -> Text(searchSuggestionRenderer.suggestion.toString())
            musicTwoColumnItemRenderer != null -> Navigation(
                title = musicTwoColumnItemRenderer.title.toString(),
                subtitle = musicTwoColumnItemRenderer.subtitle.toString(),
                thumbnail = musicTwoColumnItemRenderer.thumbnail.musicThumbnailRenderer!!.thumbnail.thumbnails,
                thumbnailCrop = musicTwoColumnItemRenderer.thumbnail.musicThumbnailRenderer.thumbnailCrop!!,
                navigationEndpoint = musicTwoColumnItemRenderer.navigationEndpoint
            )
            else -> throw UnsupportedOperationException("Unknown suggestion item type")
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
            val navigationEndpoint: NavigationEndpoint,
        )
    }
}
