package com.zionhuang.innertube.models

import com.zionhuang.innertube.utils.TimeParser
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class YTItem : YTBaseItem() {
    abstract override val id: String
    abstract val title: String
    abstract val subtitle: String?
    abstract val thumbnails: List<Thumbnail>
    abstract val menu: ItemMenu
    abstract val navigationEndpoint: NavigationEndpoint

    abstract val shareLink: String

    interface FromContent<out T : YTItem> {
        fun from(item: MusicResponsiveListItemRenderer): T?
        fun from(item: MusicTwoRowItemRenderer): T
    }
}

@Parcelize
data class SongItem(
    override val id: String,
    override val title: String,
    override val subtitle: String,
    val index: String? = null,
    val artists: List<Run>,
    val album: Link<BrowseEndpoint>? = null,
    val albumYear: Int? = null,
    val duration: Int? = null,
    override val thumbnails: List<Thumbnail>,
    override val menu: ItemMenu,
    override val navigationEndpoint: NavigationEndpoint,
) : YTItem() {
    @IgnoredOnParcel
    override val shareLink: String = "https://music.youtube.com/watch?v=$id"

    companion object : FromContent<SongItem> {
        /**
         * Subtitle configurations:
         * Video • artist • view count • length
         * artist • view count • length
         * artist • view count
         * artist • (empty)
         *
         * Note that artist's [Run] may have [navigationEndpoint] null
         */
        override fun from(item: MusicResponsiveListItemRenderer): SongItem? {
            if (item.menu == null) return null
            val menu = item.menu.toItemMenu()
            return SongItem(
                id = item.playlistItemData?.videoId
                    ?: item.flexColumns[0].musicResponsiveListItemFlexColumnRenderer.text?.runs?.firstOrNull()?.navigationEndpoint?.watchEndpoint?.videoId
                    ?: menu.radioEndpoint?.watchEndpoint?.videoId
                    ?: return null,
                title = item.getTitle(),
                subtitle = item.getSubtitle(),
                index = item.index?.toString(),
                artists = item.flexColumns[1].musicResponsiveListItemFlexColumnRenderer.text?.runs
                    ?.filter { it.navigationEndpoint?.getEndpointType() == ITEM_ARTIST }
                    ?.ifEmpty {
                        listOfNotNull(
                            if (item.fixedColumns != null) {
                                // Table style
                                item.flexColumns[1].musicResponsiveListItemFlexColumnRenderer.text?.runs?.getOrNull(0)
                            } else {
                                // From search
                                item.flexColumns[1].musicResponsiveListItemFlexColumnRenderer.text?.runs?.let {
                                    it.getOrNull(it.lastIndex - 4) ?: it.getOrNull(it.lastIndex - 2)
                                }
                            }
                        )
                    }.orEmpty(),
                album = item.flexColumns[1].musicResponsiveListItemFlexColumnRenderer.text?.runs
                    ?.find { it.navigationEndpoint?.getEndpointType() == ITEM_ALBUM }
                    ?.toLink(),
                duration = item.flexColumns[1].musicResponsiveListItemFlexColumnRenderer.text?.runs?.lastOrNull()?.text?.let { TimeParser.parse(it) }
                    ?: item.fixedColumns?.firstOrNull()?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.firstOrNull()?.text?.let { TimeParser.parse(it) },
                thumbnails = item.thumbnail?.getThumbnails().orEmpty(),
                menu = menu,
                navigationEndpoint = item.flexColumns[0].musicResponsiveListItemFlexColumnRenderer.text?.runs?.firstOrNull()?.navigationEndpoint!!
            )
        }

        override fun from(item: MusicTwoRowItemRenderer): SongItem {
            val menu = item.menu.toItemMenu()
            return SongItem(
                id = item.navigationEndpoint.watchEndpoint?.videoId
                    ?: menu.radioEndpoint?.watchEndpoint?.videoId!!,
                title = item.title.toString(),
                subtitle = item.subtitle.toString(),
                artists = emptyList(),
                thumbnails = item.thumbnailRenderer.getThumbnails(),
                menu = menu,
                navigationEndpoint = item.navigationEndpoint
            )
        }
    }
}

@Parcelize
data class AlbumItem(
    override val id: String, // browseId
    val playlistId: String,
    override val title: String,
    override val subtitle: String,
    val year: Int? = null,
    override val thumbnails: List<Thumbnail>,
    override val menu: ItemMenu,
    override val navigationEndpoint: NavigationEndpoint,
) : YTItem() {
    @IgnoredOnParcel
    override val shareLink: String = "https://music.youtube.com/playlist?list=$id"

    companion object : FromContent<AlbumItem> {
        override fun from(item: MusicResponsiveListItemRenderer): AlbumItem? {
            if (item.menu == null) return null
            val menu = item.menu.toItemMenu()
            return AlbumItem(
                id = item.navigationEndpoint!!.browseEndpoint!!.browseId,
                playlistId = menu.shuffleEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")
                    ?: menu.radioEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")
                    ?: menu.playNextEndpoint?.queueAddEndpoint?.queueTarget?.playlistId!!,
                title = item.getTitle(),
                subtitle = item.getSubtitle(),
                year = item.flexColumns.getOrNull(1)?.musicResponsiveListItemFlexColumnRenderer?.text?.runs?.lastOrNull()?.text?.toIntOrNull(),
                thumbnails = item.thumbnail!!.getThumbnails(),
                menu = menu,
                navigationEndpoint = item.navigationEndpoint
            )
        }

        override fun from(item: MusicTwoRowItemRenderer): AlbumItem {
            val menu = item.menu.toItemMenu()
            return AlbumItem(
                id = item.navigationEndpoint.browseEndpoint!!.browseId,
                playlistId = menu.shuffleEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")
                    ?: menu.radioEndpoint?.watchPlaylistEndpoint?.playlistId?.removePrefix("RDAMPL")
                    ?: menu.playNextEndpoint?.queueAddEndpoint?.queueTarget?.playlistId!!,
                title = item.title.toString(),
                subtitle = item.subtitle.toString(),
                thumbnails = item.thumbnailRenderer.getThumbnails(),
                year = item.subtitle.runs.lastOrNull()?.text?.toIntOrNull(),
                menu = menu,
                navigationEndpoint = item.navigationEndpoint
            )
        }
    }
}

@Parcelize
data class PlaylistItem(
    override val id: String,
    override val title: String,
    override val subtitle: String,
    override val thumbnails: List<Thumbnail>,
    override val menu: ItemMenu,
    override val navigationEndpoint: NavigationEndpoint,
) : YTItem() {
    @IgnoredOnParcel
    override val shareLink: String = "https://music.youtube.com/playlist?list=$id"

    companion object : FromContent<PlaylistItem> {
        override fun from(item: MusicResponsiveListItemRenderer): PlaylistItem? {
            if (item.menu == null) return null
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

@Parcelize
data class ArtistItem(
    override val id: String,
    override val title: String,
    override val subtitle: String,
    override val thumbnails: List<Thumbnail>,
    override val menu: ItemMenu,
    override val navigationEndpoint: NavigationEndpoint,
) : YTItem() {
    @IgnoredOnParcel
    override val shareLink: String = "https://music.youtube.com/channel/$id"

    companion object : FromContent<ArtistItem> {
        override fun from(item: MusicResponsiveListItemRenderer): ArtistItem? {
            if (item.menu == null) return null
            return ArtistItem(
                id = item.navigationEndpoint?.browseEndpoint?.browseId!!,
                title = item.getTitle(),
                subtitle = item.getSubtitle(),
                thumbnails = item.thumbnail!!.getThumbnails(),
                menu = item.menu.toItemMenu(),
                navigationEndpoint = item.navigationEndpoint
            )
        }

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

@Parcelize
data class NavigationItem(
    val title: String,
    override val id: String = title,
    val subtitle: String? = null,
    val icon: String? = null,
    val stripeColor: Long? = null,
    val navigationEndpoint: NavigationEndpoint,
) : YTBaseItem()

@Parcelize
data class SuggestionTextItem(
    val query: String,
    val source: SuggestionSource = SuggestionSource.YOUTUBE,
    override val id: String = query,
) : YTBaseItem() {
    enum class SuggestionSource {
        LOCAL, YOUTUBE
    }
}

@Parcelize
object Separator : YTBaseItem() {
    @IgnoredOnParcel
    override val id: String = "SEPARATOR"
}

const val ITEM_UNKNOWN = -1
const val ITEM_SONG = 0
const val ITEM_VIDEO = 1
const val ITEM_ALBUM = 2
const val ITEM_PLAYLIST = 3
const val ITEM_ARTIST = 4