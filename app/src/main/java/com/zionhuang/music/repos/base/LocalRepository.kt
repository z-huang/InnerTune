package com.zionhuang.music.repos.base

import com.zionhuang.music.db.entities.*
import com.zionhuang.music.models.base.ISortInfo
import com.zionhuang.music.models.ListWrapper
import java.io.File

interface LocalRepository {
    suspend fun getSongById(songId: String): Song?
    fun searchSongs(query: String): ListWrapper<Int, Song>
    suspend fun addSongs(songs: List<Song>)
    suspend fun addSong(song: Song) = addSongs(listOf(song))
    suspend fun updateSongs(songs: List<Song>)
    suspend fun updateSong(song: Song) = updateSongs(listOf(song))
    suspend fun moveSongsToTrash(songs: List<Song>)
    suspend fun restoreSongsFromTrash(songs: List<Song>)
    suspend fun deleteSongs(songs: List<Song>)
    suspend fun setLiked(liked: Boolean, songs: List<Song>)
    suspend fun downloadSongs(songIds: List<String>)
    suspend fun downloadSong(songId: String) = downloadSongs(listOf(songId))
    suspend fun removeDownloads(songIds: List<String>)
    suspend fun removeDownload(songId: String) = removeDownloads(listOf(songId))
    fun getSongFile(songId: String): File
    fun getSongArtworkFile(songId: String): File

    fun getAllSongs(sortInfo: ISortInfo): ListWrapper<Int, Song>
    fun getArtistSongs(artistId: Int, sortInfo: ISortInfo): ListWrapper<Int, Song>
    fun getPlaylistSongs(playlistId: Int, sortInfo: ISortInfo): ListWrapper<Int, Song>

    fun getAllArtists(): ListWrapper<Int, ArtistEntity>
    suspend fun getArtistById(artistId: Int): ArtistEntity?
    fun searchArtists(query: String): ListWrapper<Int, ArtistEntity>
    suspend fun addArtist(artist: ArtistEntity)
    suspend fun updateArtist(artist: ArtistEntity)
    suspend fun deleteArtist(artist: ArtistEntity)

    fun getAllPlaylists(): ListWrapper<Int, PlaylistEntity>
    suspend fun getPlaylistById(playlistId: Int): PlaylistEntity?
    fun searchPlaylists(query: String): ListWrapper<Int, PlaylistEntity>
    suspend fun addPlaylist(playlist: PlaylistEntity)
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    suspend fun getPlaylistSongEntities(playlistId: Int): ListWrapper<Int, PlaylistSongEntity>
    suspend fun updatePlaylistSongEntities(playlistSongEntities: List<PlaylistSongEntity>)
    suspend fun addSongsToPlaylist(playlistId: Int, songs: List<Song>)
    suspend fun removeSongsFromPlaylist(playlistId: Int, idInPlaylist: List<Int>)
    suspend fun removeSongFromPlaylist(playlistId: Int, idInPlaylist: Int) = removeSongsFromPlaylist(playlistId, listOf(idInPlaylist))

    fun getAllDownloads(): ListWrapper<Int, DownloadEntity>
    suspend fun getDownloadEntity(downloadId: Long): DownloadEntity?
    suspend fun addDownload(item: DownloadEntity)
    suspend fun removeDownloadEntity(downloadId: Long)
}