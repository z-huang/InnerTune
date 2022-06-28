package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoints.NavigationEndpoint
import kotlinx.serialization.Serializable

@Serializable
sealed class Section {
    abstract val header: Header?

    @Serializable
    data class Header(
        val title: String,
        val subtitle: String? = null,
        val moreNavigationEndpoint: NavigationEndpoint? = null,
    )
}

@Serializable
data class ItemSection(
    override val header: Header? = null,
    val items: List<Item>,
    val continuation: String? = null,
) : Section()

@Serializable
data class CarouselSection(
    override val header: Header? = null,
    val items: List<Item>,
) : Section()

@Serializable
data class GridSection(
    override val header: Header? = null,
    val items: List<Item>,
//    val moreNavigationEndpoint: BrowseEndpoint,
) : Section()

@Serializable
data class DescriptionSection(
    override val header: Header? = null,
    val description: String,
) : Section()