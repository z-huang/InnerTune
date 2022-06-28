package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicCarouselShelfRenderer(
    val header: Header,
    val contents: List<Content>,
    val itemSize: String,
) {
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
        val musicNavigationButtonRenderer: MusicNavigationButtonRenderer?, // navigation button in explore tab
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer?,
    ) {
        fun toItem() = musicTwoRowItemRenderer?.toItem()
            ?: musicNavigationButtonRenderer?.toItem()
            ?: musicResponsiveListItemRenderer?.toItem()!!
    }
}
