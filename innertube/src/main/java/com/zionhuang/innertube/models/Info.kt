package com.zionhuang.innertube.models

import com.zionhuang.innertube.models.endpoint.WatchEndpoint
import com.zionhuang.innertube.models.response.BrowseResponse

sealed class Info {
    interface FromBrowseResponse<out T : Info> {
        fun from(response: BrowseResponse): T
    }
}

data class ArtistInfo(
    val name: String,
    val description: String,
    val bannerThumbnail: List<Thumbnail>,
    val shuffleEndpoint: WatchEndpoint,
    val radioEndpoint: WatchEndpoint,
    val contents: List<Section>,
) : Info() {
    companion object : FromBrowseResponse<ArtistInfo> {
        override fun from(response: BrowseResponse): ArtistInfo = ArtistInfo(
            name = response.header!!.musicImmersiveHeaderRenderer!!.title.toString(),
            description = response.header.musicImmersiveHeaderRenderer!!.description.toString(),
            bannerThumbnail = response.header.musicImmersiveHeaderRenderer.thumbnail.getThumbnails(),
            shuffleEndpoint = response.header.musicImmersiveHeaderRenderer.playButton.buttonRenderer.navigationEndpoint.watchEndpoint!!,
            radioEndpoint = response.header.musicImmersiveHeaderRenderer.startRadioButton.buttonRenderer.navigationEndpoint.watchEndpoint!!,
            contents = response.toSectionList()
        )
    }
}

data class AlbumInfo(
    val name: String,
    val subtitle: String,
    val secondSubtitle: String,
    val description: String?,
    val items: List<SongItem>,
    val thumbnail: List<Thumbnail>,
    val menu: ItemMenu,
) : Info() {
    companion object : FromBrowseResponse<AlbumInfo> {
        override fun from(response: BrowseResponse): AlbumInfo = AlbumInfo(
            name = response.header!!.musicDetailHeaderRenderer!!.title.toString(),
            subtitle = response.header.musicDetailHeaderRenderer!!.subtitle.toString(),
            secondSubtitle = response.header.musicDetailHeaderRenderer.secondSubtitle.toString(),
            description = response.header.musicDetailHeaderRenderer.description?.toString(),
            items = response.contents.singleColumnBrowseResultsRenderer!!.tabs[0].tabRenderer.content!!.sectionListRenderer!!.contents[0].musicShelfRenderer!!.contents.map {
                SongItem.from(it.musicResponsiveListItemRenderer)
            },
            thumbnail = response.header.musicDetailHeaderRenderer.thumbnail.getThumbnails(),
            menu = response.header.musicDetailHeaderRenderer.menu.toItemMenu()
        )
    }
}

data class PlaylistInfo(
    val name: String,
    val subtitle: String,
    val secondSubtitle: String,
    val description: String,
    val thumbnail: List<Thumbnail>,
) : Info() {
    companion object : FromBrowseResponse<PlaylistInfo> {
        override fun from(response: BrowseResponse): PlaylistInfo = PlaylistInfo(
            name = response.header!!.musicDetailHeaderRenderer!!.title.toString(),
            subtitle = response.header.musicDetailHeaderRenderer!!.subtitle.toString(),
            secondSubtitle = response.header.musicDetailHeaderRenderer.secondSubtitle.toString(),
            description = response.header.musicDetailHeaderRenderer.description.toString(),
            thumbnail = response.header.musicDetailHeaderRenderer.thumbnail.getThumbnails()
        )
    }
}

fun BrowseResponse.toArtistInfo() = ArtistInfo.from(this)
fun BrowseResponse.toAlbumInfo() = AlbumInfo.from(this)
fun BrowseResponse.toPlaylistInfo() = PlaylistInfo.from(this)
