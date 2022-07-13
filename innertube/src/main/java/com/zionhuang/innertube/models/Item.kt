package com.zionhuang.innertube.models

sealed class BaseItem {
    abstract val id: String

    enum class ViewType {
        LIST, BLOCK
    }
}

sealed class Item : BaseItem() {
    abstract override val id: String
    abstract  val title: String
    abstract val subtitle: String?
    abstract val thumbnails: List<Thumbnail>
    abstract val menu: ItemMenu
    abstract val navigationEndpoint: NavigationEndpoint

    interface FromContent<out T : Item> {
        fun from(item: MusicResponsiveListItemRenderer): T
        fun from(item: MusicTwoRowItemRenderer): T
    }
}

data class SongItem(
    override val id: String,
    override val title: String,
    override val subtitle: String,
    val index: String? = null,
    override val thumbnails: List<Thumbnail>,
    override val menu: ItemMenu,
    override val navigationEndpoint: NavigationEndpoint,
) : Item() {
    companion object : FromContent<SongItem> {
        override fun from(item: MusicResponsiveListItemRenderer): SongItem {
            val menu = item.menu.toItemMenu()
            return SongItem(
                id = item.playlistItemData?.videoId
                    ?: item.flexColumns[0].musicResponsiveListItemFlexColumnRenderer.text.runs[0].navigationEndpoint?.watchEndpoint?.videoId
                    ?: menu.radioEndpoint?.watchEndpoint?.videoId!!,
                title = item.getTitle(),
                subtitle = item.getSubtitle(),
                index = item.index?.toString(),
                thumbnails = item.thumbnail?.getThumbnails().orEmpty(),
                menu = menu,
                navigationEndpoint = item.flexColumns[0].musicResponsiveListItemFlexColumnRenderer.text.runs[0].navigationEndpoint!!
            )
        }

        override fun from(item: MusicTwoRowItemRenderer): SongItem {
            val menu = item.menu.toItemMenu()
            return SongItem(
                id = item.navigationEndpoint.watchEndpoint?.videoId
                    ?: menu.radioEndpoint?.watchEndpoint?.videoId!!,
                title = item.title.toString(),
                subtitle = item.subtitle.toString(),
                thumbnails = item.thumbnailRenderer.getThumbnails(),
                menu = menu,
                navigationEndpoint = item.navigationEndpoint
            )
        }
    }
}

data class VideoItem(
    override val id: String,
    override val title: String,
    override val subtitle: String,
    override val thumbnails: List<Thumbnail>,
    override val menu: ItemMenu,
    override val navigationEndpoint: NavigationEndpoint,
) : Item() {
    companion object : FromContent<VideoItem> {
        override fun from(item: MusicResponsiveListItemRenderer): VideoItem {
            val menu = item.menu.toItemMenu()
            return VideoItem(
                id = item.playlistItemData?.videoId
                    ?: item.flexColumns[0].musicResponsiveListItemFlexColumnRenderer.text.runs[0].navigationEndpoint?.watchEndpoint?.videoId
                    ?: menu.radioEndpoint?.watchEndpoint?.videoId!!,
                title = item.getTitle(),
                subtitle = item.getSubtitle(),
                thumbnails = item.thumbnail!!.getThumbnails(),
                menu = menu,
                navigationEndpoint = item.flexColumns[0].musicResponsiveListItemFlexColumnRenderer.text.runs[0].navigationEndpoint!!
            )
        }

        override fun from(item: MusicTwoRowItemRenderer): VideoItem {
            val menu = item.menu.toItemMenu()
            return VideoItem(
                id = item.navigationEndpoint.watchEndpoint?.videoId
                    ?: menu.radioEndpoint?.watchEndpoint?.videoId!!,
                title = item.title.toString(),
                subtitle = item.subtitle.toString(),
                thumbnails = item.thumbnailRenderer.getThumbnails(),
                menu = menu,
                navigationEndpoint = item.navigationEndpoint
            )
        }
    }
}

data class AlbumItem(
    override val id: String,
    override val title: String,
    override val subtitle: String,
    override val thumbnails: List<Thumbnail>,
    override val menu: ItemMenu,
    override val navigationEndpoint: NavigationEndpoint,
) : Item() {
    companion object : FromContent<AlbumItem> {
        override fun from(item: MusicResponsiveListItemRenderer): AlbumItem {
            val menu = item.menu.toItemMenu()
            return AlbumItem(
                id = menu.shuffleEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")
                    ?: menu.radioEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")!!,
                title = item.getTitle(),
                subtitle = item.getSubtitle(),
                thumbnails = item.thumbnail!!.getThumbnails(),
                menu = menu,
                navigationEndpoint = item.navigationEndpoint!!
            )
        }

        override fun from(item: MusicTwoRowItemRenderer): AlbumItem {
            val menu = item.menu.toItemMenu()
            return AlbumItem(
                id = menu.shuffleEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")
                    ?: menu.radioEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")!!,
                title = item.title.toString(),
                subtitle = item.subtitle.toString(),
                thumbnails = item.thumbnailRenderer.getThumbnails(),
                menu = menu,
                navigationEndpoint = item.navigationEndpoint
            )
        }
    }
}

data class PlaylistItem(
    override val id: String,
    override val title: String,
    override val subtitle: String,
    override val thumbnails: List<Thumbnail>,
    override val menu: ItemMenu,
    override val navigationEndpoint: NavigationEndpoint,
) : Item() {
    companion object : FromContent<PlaylistItem> {
        override fun from(item: MusicResponsiveListItemRenderer): PlaylistItem {
            val menu = item.menu.toItemMenu()
            return PlaylistItem(
                id = item.navigationEndpoint?.browseEndpoint?.browseId?.removePrefix("VL")
                    ?: menu.shuffleEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")
                    ?: menu.radioEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")!!,
                title = item.getTitle(),
                subtitle = item.getSubtitle(),
                thumbnails = item.thumbnail!!.getThumbnails(),
                menu = menu,
                navigationEndpoint = item.navigationEndpoint!!
            )
        }

        override fun from(item: MusicTwoRowItemRenderer): PlaylistItem {
            val menu = item.menu.toItemMenu()
            return PlaylistItem(
                id = item.navigationEndpoint.browseEndpoint?.browseId?.removePrefix("VL")
                    ?: item.title.runs.getOrNull(0)?.navigationEndpoint?.browseEndpoint?.browseId?.removePrefix("VL")
                    ?: menu.shuffleEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")
                    ?: menu.radioEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")!!,
                title = item.title.toString(),
                subtitle = item.subtitle.toString(),
                thumbnails = item.thumbnailRenderer.getThumbnails(),
                menu = menu,
                navigationEndpoint = item.navigationEndpoint
            )
        }
    }
}

data class ArtistItem(
    override val id: String,
    override val title: String,
    override val subtitle: String,
    override val thumbnails: List<Thumbnail>,
    override val menu: ItemMenu,
    override val navigationEndpoint: NavigationEndpoint,
) : Item() {
    companion object : FromContent<ArtistItem> {
        override fun from(item: MusicResponsiveListItemRenderer): ArtistItem = ArtistItem(
            id = item.navigationEndpoint?.browseEndpoint?.browseId!!,
            title = item.getTitle(),
            subtitle = item.getSubtitle(),
            thumbnails = item.thumbnail!!.getThumbnails(),
            menu = item.menu.toItemMenu(),
            navigationEndpoint = item.navigationEndpoint
        )

        override fun from(item: MusicTwoRowItemRenderer): ArtistItem = ArtistItem(
            id = item.navigationEndpoint.browseEndpoint?.browseId!!,
            title = item.title.toString(),
            subtitle = item.subtitle.toString(),
            thumbnails = item.thumbnailRenderer.getThumbnails(),
            menu = item.menu.toItemMenu(),
            navigationEndpoint = item.navigationEndpoint
        )
    }
}

data class NavigationItem(
    val title: String,
    override val id: String = title,
    val subtitle: String? = null,
    val icon: String?,
    val stripeColor: Long?,
    val navigationEndpoint: NavigationEndpoint,
) : BaseItem()

data class SuggestionTextItem(
    val query: String,
    override val id: String = query,
) : BaseItem()

object Separator : BaseItem() {
    override val id: String = "SEPARATOR"
}

const val ITEM_UNKNOWN = -1
const val ITEM_SONG = 0
const val ITEM_VIDEO = 1
const val ITEM_ALBUM = 2
const val ITEM_PLAYLIST = 3
const val ITEM_ARTIST = 4