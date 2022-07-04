package com.zionhuang.innertube.models

sealed class Section {
    abstract val id: String

    enum class ViewType {
        LIST, BLOCK
    }
}

data class Header(
    val title: String,
    val subtitle: String? = null,
    val moreNavigationEndpoint: NavigationEndpoint? = null,
    override val id: String = title,
) : Section()

data class ArtistHeader(
    override val id: String,
    val name: String,
    val description: String?,
    val bannerThumbnail: List<Thumbnail>,
    val shuffleEndpoint: NavigationEndpoint,
    val radioEndpoint: NavigationEndpoint,
) : Section()

data class AlbumOrPlaylistHeader(
    override val id: String,
    val name: String,
    val subtitle: String,
    val secondSubtitle: String,
    val description: String?,
    val thumbnail: List<Thumbnail>,
    val menu: ItemMenu,
) : Section()

data class ListSection(
    override val id: String,
    val items: List<BaseItem>,
    val continuation: String? = null,
    val itemViewType: ViewType,
) : Section()

data class CarouselSection(
    override val id: String,
    val items: List<BaseItem>,
    val numItemsPerColumn: Int = 1,
    val itemViewType: ViewType,
) : Section()

data class GridSection(
    override val id: String,
    val items: List<BaseItem>,
//    val moreNavigationEndpoint: BrowseEndpoint,
) : Section()

data class DescriptionSection(
    override val id: String = "DESCRIPTION",
    val description: String,
) : Section()