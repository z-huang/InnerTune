package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoints.BrowseEndpoint
import com.zionhuang.innertube.models.endpoints.NavigationEndpoint
import kotlinx.serialization.Serializable

@Serializable
sealed class Item {
    abstract val title: String
    abstract val navigationEndpoint: NavigationEndpoint

    interface FromContent<out T : Item> {
        fun from(item: MusicResponsiveListItemRenderer): T
        fun from(item: MusicTwoRowItemRenderer): T
    }
}

@Serializable
data class SongItem(
    override val title: String,
    val subtitle: String,
    val index: String? = null,
    val artistEndpoint: BrowseEndpoint?,
    val albumEndpoint: BrowseEndpoint?,
    val thumbnails: List<Thumbnail>,
    override val navigationEndpoint: NavigationEndpoint,
) : Item() {
    companion object : FromContent<SongItem> {
        override fun from(item: MusicResponsiveListItemRenderer): SongItem {
            return SongItem(
                title = item.getTitle(),
                subtitle = item.getSubtitle(),
                index = item.index?.toString(),
                artistEndpoint = item.menu.getArtistEndpoint(),
                albumEndpoint = item.menu.getAlbumEndpoint(),
                thumbnails = item.thumbnail?.getThumbnails().orEmpty(),
                navigationEndpoint = item.flexColumns[0].musicResponsiveListItemFlexColumnRenderer.text.runs[0].navigationEndpoint!!
            )
        }

        override fun from(item: MusicTwoRowItemRenderer): SongItem = SongItem(
            title = item.title.toString(),
            subtitle = item.subtitle.toString(),
            artistEndpoint = item.menu.getArtistEndpoint(),
            albumEndpoint = item.menu.getAlbumEndpoint(),
            thumbnails = item.thumbnailRenderer.getThumbnails(),
            navigationEndpoint = item.navigationEndpoint
        )
    }
}

@Serializable
data class VideoItem(
    override val title: String,
    val subtitle: String,
    val artistEndpoint: BrowseEndpoint?,
    val albumEndpoint: BrowseEndpoint?,
    val thumbnails: List<Thumbnail>,
    override val navigationEndpoint: NavigationEndpoint,
) : Item() {
    companion object : FromContent<VideoItem> {
        override fun from(item: MusicResponsiveListItemRenderer): VideoItem = VideoItem(
            title = item.getTitle(),
            subtitle = item.getSubtitle(),
            artistEndpoint = item.menu.getArtistEndpoint(), // fallback: get by subtitle
            albumEndpoint = item.menu.getAlbumEndpoint(),
            thumbnails = item.thumbnail!!.getThumbnails(),
            navigationEndpoint = item.flexColumns[0].musicResponsiveListItemFlexColumnRenderer.text.runs[0].navigationEndpoint!!
        )

        override fun from(item: MusicTwoRowItemRenderer): VideoItem = VideoItem(
            title = item.title.toString(),
            subtitle = item.subtitle.toString(),
            artistEndpoint = item.menu.getArtistEndpoint(),
            albumEndpoint = item.menu.getAlbumEndpoint(),
            thumbnails = item.thumbnailRenderer.getThumbnails(),
            navigationEndpoint = item.navigationEndpoint
        )
    }
}

@Serializable
data class AlbumItem(
    override val title: String,
    val subtitle: String,
    val shuffleEndpoint: NavigationEndpoint,
    val radioEndpoint: NavigationEndpoint,
    val artistEndpoint: BrowseEndpoint?,
    val thumbnails: List<Thumbnail>,
    override val navigationEndpoint: NavigationEndpoint,
) : Item() {
    companion object : FromContent<AlbumItem> {
        override fun from(item: MusicResponsiveListItemRenderer): AlbumItem = AlbumItem(
            title = item.getTitle(),
            subtitle = item.getSubtitle(),
            shuffleEndpoint = item.menu.getShuffleEndpoint()!!,
            radioEndpoint = item.menu.getRadioEndpoint()!!,
            artistEndpoint = item.menu.getArtistEndpoint(),
            thumbnails = item.thumbnail!!.getThumbnails(),
            navigationEndpoint = item.navigationEndpoint!!
        )

        override fun from(item: MusicTwoRowItemRenderer): AlbumItem = AlbumItem(
            title = item.title.toString(),
            subtitle = item.subtitle.toString(),
            shuffleEndpoint = item.menu.getShuffleEndpoint()!!,
            radioEndpoint = item.menu.getRadioEndpoint()!!,
            artistEndpoint = item.menu.getArtistEndpoint(),
            thumbnails = item.thumbnailRenderer.getThumbnails(),
            navigationEndpoint = item.navigationEndpoint
        )
    }
}

@Serializable
data class PlaylistItem(
    override val title: String,
    val subtitle: String,
    val shuffleEndpoint: NavigationEndpoint,
    val radioEndpoint: NavigationEndpoint,
    val thumbnails: List<Thumbnail>,
    override val navigationEndpoint: NavigationEndpoint,
) : Item() {
    companion object : FromContent<PlaylistItem> {
        override fun from(item: MusicResponsiveListItemRenderer): PlaylistItem = PlaylistItem(
            title = item.getTitle(),
            subtitle = item.getSubtitle(),
            shuffleEndpoint = item.menu.getShuffleEndpoint()!!,
            radioEndpoint = item.menu.getRadioEndpoint()!!,
            thumbnails = item.thumbnail!!.getThumbnails(),
            navigationEndpoint = item.navigationEndpoint!!
        )

        override fun from(item: MusicTwoRowItemRenderer): PlaylistItem = PlaylistItem(
            title = item.title.toString(),
            subtitle = item.subtitle.toString(),
            shuffleEndpoint = item.menu.getShuffleEndpoint()!!,
            radioEndpoint = item.menu.getRadioEndpoint()!!,
            thumbnails = item.thumbnailRenderer.getThumbnails(),
            navigationEndpoint = item.navigationEndpoint
        )
    }
}

@Serializable
data class ArtistItem(
    override val title: String,
    val subtitle: String,
    val shuffleEndpoint: NavigationEndpoint,
    val radioEndpoint: NavigationEndpoint,
    val thumbnails: List<Thumbnail>,
    override val navigationEndpoint: NavigationEndpoint,
) : Item() {
    companion object : FromContent<ArtistItem> {
        override fun from(item: MusicResponsiveListItemRenderer): ArtistItem = ArtistItem(
            title = item.getTitle(),
            subtitle = item.getSubtitle(),
            shuffleEndpoint = item.menu.getShuffleEndpoint()!!,
            radioEndpoint = item.menu.getRadioEndpoint()!!,
            thumbnails = item.thumbnail!!.getThumbnails(),
            navigationEndpoint = item.navigationEndpoint!!
        )

        override fun from(item: MusicTwoRowItemRenderer): ArtistItem {
            return ArtistItem(
                title = item.title.toString(),
                subtitle = item.subtitle.toString(),
                shuffleEndpoint = item.menu.getShuffleEndpoint()!!,
                radioEndpoint = item.menu.getRadioEndpoint()!!,
                thumbnails = item.thumbnailRenderer.getThumbnails(),
                navigationEndpoint = item.navigationEndpoint
            )
        }
    }
}

@Serializable
data class NavigationItem(
    override val title: String,
    val icon: String?,
    val stripeColor: Long?,
    override val navigationEndpoint: NavigationEndpoint,
) : Item()
