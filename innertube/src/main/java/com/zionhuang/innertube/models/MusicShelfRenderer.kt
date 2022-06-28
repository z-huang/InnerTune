package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoints.NavigationEndpoint
import kotlinx.serialization.Serializable

@Serializable
data class MusicShelfRenderer(
    val title: Runs?,
    val contents: List<Content>,
    val bottomEndpoint: NavigationEndpoint?,
    val moreContentButton: MoreContentButton?,
    val continuations: List<Continuation>?,
) {
    fun getViewType() = Section.ViewType.LIST

    @Serializable
    data class Content(
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer,
    ) {
        fun toItem(): Item = musicResponsiveListItemRenderer.toItem()
    }

    @Serializable
    data class MoreContentButton(
        val buttonRenderer: Button,
    )
}