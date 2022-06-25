package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoints.BrowseEndpoint
import com.zionhuang.innertube.models.endpoints.SearchEndpoint
import kotlinx.serialization.Serializable

@Serializable
sealed class Section

@Serializable
data class ItemSection(
    val title: String? = null,
    val items: List<Item>,
    val continuation: String? = null,
    val bottomEndpoint: SearchEndpoint? = null,
) : Section()

@Serializable
data class DescriptionSection(
    val title: String,
    val subtitle: String,
    val description: String,
) : Section()

@Serializable
data class LinkSection(
    val navigationButtons: List<MusicNavigationButtonRenderer>,
) : Section()

@Serializable
data class GridSection(
    val title: String,
    val navigationButtons: List<MusicNavigationButtonRenderer>,
    val moreNavigationEndpoint: BrowseEndpoint,
) : Section()