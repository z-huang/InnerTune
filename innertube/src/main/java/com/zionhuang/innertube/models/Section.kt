package com.zionhuang.innertube.models

data class Header(
    val title: String,
    val subtitle: String? = null,
    val moreNavigationEndpoint: NavigationEndpoint? = null,
    override val id: String = title,
) : BaseItem()

data class ArtistHeader(
    override val id: String,
    val name: String,
    val description: String?,
    val bannerThumbnails: List<Thumbnail>,
    val shuffleEndpoint: NavigationEndpoint,
    val radioEndpoint: NavigationEndpoint,
) : BaseItem()

data class AlbumOrPlaylistHeader(
    override val id: String,
    val name: String,
    val subtitle: String,
    val secondSubtitle: String,
    val description: String?,
    val thumbnails: List<Thumbnail>,
    val menu: ItemMenu,
) : BaseItem()

data class CarouselSection(
    override val id: String,
    val items: List<BaseItem>,
    val numItemsPerColumn: Int = 1,
    val itemViewType: ViewType,
) : BaseItem()

data class GridSection(
    override val id: String,
    val items: List<BaseItem>,
//    val moreNavigationEndpoint: BrowseEndpoint,
) : BaseItem()

data class DescriptionSection(
    override val id: String = "DESCRIPTION",
    val description: String,
) : BaseItem()