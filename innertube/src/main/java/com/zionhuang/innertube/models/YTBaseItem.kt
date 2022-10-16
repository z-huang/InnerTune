package com.zionhuang.innertube.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class YTBaseItem : Parcelable {
    abstract val id: String

    enum class ViewType {
        LIST, BLOCK
    }
}

@Parcelize
data class Header(
    val title: String,
    val subtitle: String? = null,
    val moreNavigationEndpoint: NavigationEndpoint? = null,
    override val id: String = title,
) : YTBaseItem()

@Parcelize
data class ArtistHeader(
    override val id: String,
    val name: String,
    val description: String?,
    val bannerThumbnails: List<Thumbnail>?,
    val shuffleEndpoint: NavigationEndpoint?,
    val radioEndpoint: NavigationEndpoint?,
) : YTBaseItem()

@Parcelize
data class AlbumOrPlaylistHeader(
    override val id: String,
    val name: String,
    val subtitle: String,
    val secondSubtitle: String,
    val description: String?,
    val artists: List<Run>?,
    val year: Int?,
    val thumbnails: List<Thumbnail>,
    val menu: ItemMenu,
) : YTBaseItem()

@Parcelize
data class CarouselSection(
    override val id: String,
    val items: List<YTBaseItem>,
    val numItemsPerColumn: Int = 1,
    val itemViewType: ViewType,
) : YTBaseItem()

@Parcelize
data class GridSection(
    override val id: String,
    val items: List<YTBaseItem>,
//    val moreNavigationEndpoint: BrowseEndpoint,
) : YTBaseItem()

@Parcelize
data class DescriptionSection(
    override val id: String = "DESCRIPTION",
    val description: String,
) : YTBaseItem()