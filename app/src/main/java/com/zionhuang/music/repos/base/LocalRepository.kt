package com.zionhuang.music.repos.base

import com.zionhuang.music.db.entities.*
import com.zionhuang.music.models.DataWrapper
import com.zionhuang.music.models.ListWrapper
import com.zionhuang.music.models.base.ISortInfo
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

interface LocalRepository {
    suspend fun getSongById(songId: String): Song?
    fun searchSongs(query: String): ListWrapper<Int, Song>
    fun hasSong(songId: String): DataWrapper<Boolean>
    suspend fun moveToTrash(songs: List<Song>)
    suspend fun restoreFromTrash(songs: List<Song>)
    suspend fun deleteSongs(songs: List<Song>)
    suspend fun setLiked(liked: Boolean, songs: List<Song>)
    suspend fun downloadSongs(songs: List<Song>)
    suspend fun downloadSong(song: Song) = downloadSongs(listOf(song))
    suspend fun removeDownloads(songs: List<Song>)
    suspend fun removeDownload(song: Song) = removeDownloads(listOf(song))
    fun getSongFile(songId: String): File
    fun getSongArtworkFile(songId: String): File

    fun getAllSongs(sortInfo: ISortInfo): ListWrapper<Int, Song>
    fun getArtistSongs(artistId: String, sortInfo: ISortInfo): ListWrapper<Int, Song>
    fun getPlaylistSongs(playlistId: String, sortInfo: ISortInfo): ListWrapper<Int, Song>

    fun getAllArtists(): ListWrapper<Int, Artist>
    suspend fun getArtistById(artistId: String): ArtistEntity?
    suspend fun getArtistByName(name: String): ArtistEntity?
    fun searchArtists(query: String): ListWrapper<Int, ArtistEntity>
    suspend fun addArtist(artist: ArtistEntity)
    suspend fun updateArtist(artist: ArtistEntity)
    suspend fun deleteArtist(artist: ArtistEntity)

    fun getAllAlbums(): ListWrapper<Int, Album>

    fun getAllPlaylists(): ListWrapper<Int, Playlist>
    suspend fun getPlaylistById(playlistId: String): PlaylistEntity?
    fun searchPlaylists(query: String): ListWrapper<Int, PlaylistEntity>
    suspend fun addPlaylist(playlist: PlaylistEntity)
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    suspend fun deletePlaylist(playlist: PlaylistEntity)

    suspend fun getPlaylistSongEntities(playlistId: String): ListWrapper<Int, PlaylistSongMap>
    suspend fun updatePlaylistSongEntities(playlistSongEntities: List<PlaylistSongMap>)
    suspend fun addSongsToPlaylist(playlistId: String, songs: List<Song>)
    suspend fun removeSongsFromPlaylist(playlistId: String, idInPlaylist: List<Int>)
    suspend fun removeSongFromPlaylist(playlistId: String, idInPlaylist: Int) = removeSongsFromPlaylist(playlistId, listOf(idInPlaylist))

    fun getAllDownloads(): ListWrapper<Int, DownloadEntity>
    suspend fun getDownloadEntity(downloadId: Long): DownloadEntity?
    suspend fun addDownload(item: DownloadEntity)
    suspend fun removeDownloadEntity(downloadId: Long)

    suspend fun getAllSearchHistory(): List<SearchHistory>
    suspend fun getSearchHistory(query: String): List<SearchHistory>
    suspend fun insertSearchHistory(query: String)
    suspend fun deleteSearchHistory(query: String)
}