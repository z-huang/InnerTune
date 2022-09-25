package com.zionhuang.music.db.entities

import com.zionhuang.music.constants.Constants.ALBUM_HEADER_ID
import com.zionhuang.music.constants.Constants.ARTIST_HEADER_ID
import com.zionhuang.music.constants.Constants.DOWNLOADED_PLAYLIST_ID
import com.zionhuang.music.constants.Constants.LIKED_PLAYLIST_ID
import com.zionhuang.music.constants.Constants.PLAYLIST_HEADER_ID
import com.zionhuang.music.constants.Constants.PLAYLIST_SONG_HEADER_ID
import com.zionhuang.music.constants.Constants.SONG_HEADER_ID
import com.zionhuang.music.constants.Constants.TEXT_HEADER_ID
import com.zionhuang.music.models.sortInfo.*

sealed class LocalBaseItem {
    abstract val id: String
}

sealed class LocalItem : LocalBaseItem()

data class SongHeader(
    val songCount: Int,
    val sortInfo: SortInfo<SongSortType>,
) : LocalBaseItem() {
    override val id = SONG_HEADER_ID
}

data class ArtistHeader(
    val artistCount: Int,
    val sortInfo: SortInfo<ArtistSortType>,
) : LocalBaseItem() {
    override val id = ARTIST_HEADER_ID
}

data class AlbumHeader(
    val albumCount: Int,
    val sortInfo: SortInfo<AlbumSortType>,
) : LocalBaseItem() {
    override val id = ALBUM_HEADER_ID
}

data class PlaylistHeader(
    val playlistCount: Int,
    val sortInfo: SortInfo<PlaylistSortType>,
) : LocalBaseItem() {
    override val id = PLAYLIST_HEADER_ID
}

data class LikedPlaylist(
    val songCount: Int,
) : LocalBaseItem() {
    override val id: String = LIKED_PLAYLIST_ID
}

data class DownloadedPlaylist(
    val songCount: Int,
) : LocalBaseItem() {
    override val id: String = DOWNLOADED_PLAYLIST_ID
}

data class PlaylistSongHeader(
    val songCount: Int,
    val length: Long,
) : LocalBaseItem() {
    override val id: String = PLAYLIST_SONG_HEADER_ID
}

data class TextHeader(
    val title: String,
) : LocalBaseItem() {
    override val id = TEXT_HEADER_ID
}