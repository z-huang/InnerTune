package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
data class Icon(
    val iconType: String,
) {
    companion object {
        const val ICON_MUSIC_NEW_RELEASE = "MUSIC_NEW_RELEASE"
        const val ICON_TRENDING_UP = "TRENDING_UP"
        const val ICON_STICKER_EMOTICON = "STICKER_EMOTICON"
        const val ICON_EXPLORE = "EXPLORE" // custom icon
    }
}