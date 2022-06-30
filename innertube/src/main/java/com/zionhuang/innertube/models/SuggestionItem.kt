package com.zionhuang.innertube.models

import kotlinx.serialization.Serializable

@Serializable
sealed class SuggestionItem

@Serializable
data class Text(
    val suggestion: String,
) : SuggestionItem()

@Serializable
data class Navigation(
    val title: String,
    val subtitle: String,
    val thumbnail: List<Thumbnail>,
    val thumbnailCrop: String,
    val navigationEndpoint: NavigationEndpoint,
) : SuggestionItem()