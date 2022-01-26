package com.zionhuang.music.repos

import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.db.entities.Song

interface LocalRepository {
    fun searchSongs(query: String): ListWrapper<Int, Song>
    fun getSongById(songId: String): Song
    fun getArtistSongs(artistId: String): ListWrapper<Int, Song>
    fun getPlaylistSongs(playlistId: String): ListWrapper<Int, Song>

    fun searchArtists(query: String): ListWrapper<Int, ArtistEntity>
    fun getArtistById(artistId: String): ArtistEntity

    fun searchPlaylists(query: String): ListWrapper<Int, PlaylistEntity>
    fun getPlaylistById(playlistId: String): PlaylistEntity
}