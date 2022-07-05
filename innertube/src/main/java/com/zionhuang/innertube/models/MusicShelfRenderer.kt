package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class MusicShelfRenderer(
    val title: Runs?,
    val contents: List<Content>?,
    val bottomEndpoint: NavigationEndpoint?,
    val moreContentButton: Button?,
    val continuations: List<Continuation>?,
) {
    fun getViewType() = Section.ViewType.LIST

    fun toSectionHeader() = title?.let {
        Header(
            title = it.toString(),
            moreNavigationEndpoint = bottomEndpoint ?: moreContentButton?.buttonRenderer?.navigationEndpoint
        )
    }

    @Serializable
    data class Content(
        val musicResponsiveListItemRenderer: MusicResponsiveListItemRenderer,
    ) {
        fun toItem(): Item = musicResponsiveListItemRenderer.toItem()
    }
}