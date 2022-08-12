package com.zionhuang.innertube.models

data class Header(
    val title: String,
    val subtitle: String? = null,
    val moreNavigationEndpoint: NavigationEndpoint? = null,
    override val id: String = title,
) : YTBaseItem()

data class ArtistHeader(
    override val id: String,
    val name: String,
    val description: String?,
    val bannerThumbnails: List<Thumbnail>,
    val shuffleEndpoint: NavigationEndpoint?,
    val radioEndpoint: NavigationEndpoint?,
) : YTBaseItem()

data class AlbumOrPlaylistHeader(
    override val id: String,
    val name: String,
    val subtitle: String,
    val secondSubtitle: String,
    val description: String?,
    val thumbnails: List<Thumbnail>,
    val menu: ItemMenu,
) : YTBaseItem()

data class CarouselSection(
    override val id: String,
    val items: List<YTBaseItem>,
    val numItemsPerColumn: Int = 1,
    val itemViewType: ViewType,
) : YTBaseItem()

data class GridSection(
    override val id: String,
    val items: List<YTBaseItem>,
//    val moreNavigationEndpoint: BrowseEndpoint,
) : YTBaseItem()

data class DescriptionSection(
    override val id: String = "DESCRIPTION",
    val description: String,
) : YTBaseItem()