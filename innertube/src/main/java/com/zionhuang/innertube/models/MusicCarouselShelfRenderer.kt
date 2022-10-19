package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicCarouselShelfRenderer(
    val header: Header?,
    val contents: List<Content>,
    val itemSize: String,
    val numItemsPerColumn: Int?,
) {
    fun getViewType() = when {
        contents[0].musicTwoRowItemRenderer != null -> YTBaseItem.ViewType.BLOCK
        contents[0].musicResponsiveListItemRenderer != null -> YTBaseItem.ViewType.LIST
        contents[0].musicNavigationButtonRenderer != null -> YTBaseItem.ViewType.BLOCK
        else -> YTBaseItem.ViewType.LIST
    }

    @Serializable
    data class Header(
        val musicCarouselShelfBasicHeaderRenderer: MusicCarouselShelfBasicHeaderRenderer,
    ) {
        @Serializable
        data class MusicCarouselShelfBasicHeaderRenderer(
            val strapline: Runs?,
            val title: Runs,
            val thumbnail: ThumbnailRenderer?,
            val moreContentButton: Button?,
        )

        fun toHeader() = com.zionhuang.innertube.models.Header(
            title = musicCarouselShelfBasicHeaderRenderer.title.toString(),
            moreNavigationEndpoint = musicCarouselShelfBasicHeaderRenderer.moreContentButton?.buttonRenderer?.navigationEndpoint
        )
    }

    @Serializable
    data class Content(
        val musicTwoRowItemRenderer: MusicTwoRowItemRenderer?,
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer?,
        val musicNavigationButtonRenderer: MusicNavigationButtonRenderer?, // navigation button in explore tab
    ) {
        fun toBaseItem(): YTBaseItem? = musicTwoRowItemRenderer?.toItem()
            ?: musicResponsiveListItemRenderer?.toItem()
            ?: musicNavigationButtonRenderer?.toItem()
    }
}
