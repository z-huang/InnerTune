package com.zionhuang.innertube.pages

import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.innertube.models.SectionListRenderer

data class MoodAndGenres(
    val title: String,
    val items: List<Item>,
) {
    data class Item(
        val title: String,
        val stripeColor: Long,
        val endpoint: BrowseEndpoint,
    )

    companion object {
        fun fromSectionListRendererContent(content: SectionListRenderer.Content): MoodAndGenres? {
            return MoodAndGenres(
                title = content.gridRenderer?.header?.gridHeaderRenderer?.title?.runs?.firstOrNull()?.text ?: return null,
                items = content.gridRenderer.items.map {
                    Item(
                        title = it.musicNavigationButtonRenderer?.buttonText?.runs?.firstOrNull()?.text ?: return null,
                        stripeColor = it.musicNavigationButtonRenderer.solid?.leftStripeColor ?: return null,
                        endpoint = it.musicNavigationButtonRenderer.clickCommand.browseEndpoint ?: return null,
                    )
                }
            )
        }
    }
}
