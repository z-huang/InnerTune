package com.zionhuang.music.repos.base

import com.zionhuang.music.db.entities.*
import com.zionhuang.music.models.*
import com.zionhuang.music.models.sortInfo.*
import java.io.File

interface LocalRepository {
    suspend fun getSongById(songId: String): Song?
    fun searchSongs(query: String): ListWrapper<Int, Song>
    fun hasSong(songId: String): DataWrapper<Boolean>
    suspend fun moveToTrash(songs: List<Song>)
    suspend fun restoreFromTrash(songs: List<Song>)
    suspend fun deleteSongs(songs: List<Song>)
    suspend fun setLiked(liked: Boolean, songs: List<Song>)
    suspend fun downloadSongs(songs: List<SongEntity>)
    suspend fun downloadSong(song: SongEntity) = downloadSongs(listOf(song))
    suspend fun removeDownloads(songs: List<Song>)
    suspend fun removeDownload(song: Song) = removeDownloads(listOf(song))
    fun getSongFile(songId: String): File
    fun getSongArtworkFile(songId: String): File

    fun getAllSongs(sortInfo: ISortInfo<SongSortType>): ListWrapper<Int, Song>
    fun getArtistSongs(artistId: String, sortInfo: ISortInfo<SongSortType>): ListWrapper<Int, Song>
    fun getPlaylistSongs(playlistId: String, sortInfo: ISortInfo<SongSortType>): ListWrapper<Int, Song>

    fun getAllArtists(sortInfo: ISortInfo<ArtistSortType>): ListWrapper<Int, Artist>
    suspend fun getArtistById(artistId: String): ArtistEntity?
    suspend fun getArtistByName(name: String): ArtistEntity?
    fun searchArtists(query: String): ListWrapper<Int, ArtistEntity>
    suspend fun addArtist(artist: ArtistEntity)
    suspend fun updateArtist(artist: ArtistEntity)
    suspend fun deleteArtist(artist: ArtistEntity) = deleteArtists(listOf(artist))
    suspend fun deleteArtists(artists: List<ArtistEntity>)

    fun getAllAlbums(sortInfo: ISortInfo<AlbumSortType>): ListWrapper<Int, Album>

    fun getAllPlaylists(sortInfo: ISortInfo<PlaylistSortType>): ListWrapper<Int, Playlist>
    fun searchPlaylists(query: String): ListWrapper<Int, PlaylistEntity>
    suspend fun addPlaylist(playlist: PlaylistEntity)
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    suspend fun deletePlaylists(playlists: List<PlaylistEntity>)

    suspend fun getPlaylistSongEntities(playlistId: String): ListWrapper<Int, PlaylistSongMap>
    suspend fun updatePlaylistSongEntities(playlistSongEntities: List<PlaylistSongMap>)
    suspend fun addSongsToPlaylist(playlistId: String, songIds: List<String>)
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