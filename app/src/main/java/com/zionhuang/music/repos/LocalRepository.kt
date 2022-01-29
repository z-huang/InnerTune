package com.zionhuang.music.repos

import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.models.SortInfo

interface LocalRepository {
    fun searchSongs(query: String): ListWrapper<Int, Song>
    suspend fun getSongById(songId: String): Song?
    fun getArtistSongs(artistId: Int, sortInfo: SortInfo): ListWrapper<Int, Song>
    fun getPlaylistSongs(playlistId: Int, sortInfo: SortInfo): ListWrapper<Int, Song>

    fun searchArtists(query: String): ListWrapper<Int, ArtistEntity>
    suspend fun getArtistById(artistId: Int): ArtistEntity?

    fun searchPlaylists(query: String): ListWrapper<Int, PlaylistEntity>
    suspend fun getPlaylistById(playlistId: Int): PlaylistEntity?
}