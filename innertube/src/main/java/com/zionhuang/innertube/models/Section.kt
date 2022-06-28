package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoints.NavigationEndpoint
import kotlinx.serialization.Serializable

@Serializable
sealed class Section {
    abstract val header: Header?

    enum class ViewType {
        LIST, BLOCK
    }

    @Serializable
    data class Header(
        val title: String,
        val subtitle: String? = null,
        val moreNavigationEndpoint: NavigationEndpoint? = null,
    )
}

@Serializable
data class ListSection(
    override val header: Header? = null,
    val items: List<Item>,
    val continuation: String? = null,
    val itemViewType: ViewType,
) : Section()

@Serializable
data class CarouselSection(
    override val header: Header? = null,
    val items: List<Item>,
    val numItemsPerColumn: Int = 1,
    val itemViewType: ViewType,
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