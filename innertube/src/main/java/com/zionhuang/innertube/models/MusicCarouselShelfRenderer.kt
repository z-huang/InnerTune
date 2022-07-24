package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicCarouselShelfRenderer(
    val header: Header,
    val contents: List<Content>,
    val itemSize: String,
    val numItemsPerColumn: Int?,
) {
    fun getViewType() = when {
        contents[0].musicTwoRowItemRenderer != null -> BaseItem.ViewType.BLOCK
        contents[0].musicResponsiveListItemRenderer != null -> BaseItem.ViewType.LIST
        contents[0].musicNavigationButtonRenderer != null -> BaseItem.ViewType.BLOCK
        else -> BaseItem.ViewType.LIST
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
    }

    @Serializable
    data class Content(
        val musicTwoRowItemRenderer: MusicTwoRowItemRenderer?,
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer?,
        val musicNavigationButtonRenderer: MusicNavigationButtonRenderer?, // navigation button in explore tab
    ) {
        fun toBaseItem(): BaseItem? = musicTwoRowItemRenderer?.toItem()
            ?: musicResponsiveListItemRenderer?.toItem()
            ?: musicNavigationButtonRenderer?.toItem()
    }
}
