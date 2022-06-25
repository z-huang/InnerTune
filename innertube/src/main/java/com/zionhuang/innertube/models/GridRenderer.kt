package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class GridRenderer(
    val items: List<Item>,
) {
    @Serializable
    data class Item(
        val musicNavigationButtonRenderer: MusicNavigationButtonRenderer?,
        val musicTwoRowItemRenderer: MusicTwoRowItemRenderer?,
    ) {
        fun toItem() = musicNavigationButtonRenderer?.toItem() ?: musicTwoRowItemRenderer?.toItem()!!
    }
}