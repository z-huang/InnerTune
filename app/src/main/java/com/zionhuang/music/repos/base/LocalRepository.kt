package com.zionhuang.music.repos.base

import com.zionhuang.innertube.models.*
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.models.DataWrapper
import com.zionhuang.music.models.ListWrapper
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.models.sortInfo.*
import kotlinx.coroutines.flow.Flow
import java.io.File

interface LocalRepository {
    /**
     * Browse
     */
    fun getAllSongs(sortInfo: ISortInfo<SongSortType>): ListWrapper<Int, Song>
    suspend fun getSongCount(): Int

    fun getAllArtists(sortInfo: ISortInfo<ArtistSortType>): ListWrapper<Int, Artist>
    suspend fun getArtistCount(): Int

    suspend fun getArtistSongsPreview(artistId: String): List<YTBaseItem>
    fun getArtistSongs(artistId: String, sortInfo: ISortInfo<SongSortType>): ListWrapper<Int, Song>
    suspend fun getArtistSongCount(artistId: String): Int

    fun getAllAlbums(sortInfo: ISortInfo<AlbumSortType>): ListWrapper<Int, Album>
    suspend fun getAlbumCount(): Int
    suspend fun getAlbumSongs(albumId: String): List<Song>

    fun getAllPlaylists(sortInfo: ISortInfo<PlaylistSortType>): ListWrapper<Int, Playlist>
    suspend fun getPlaylistCount(): Int

    fun getPlaylistSongs(playlistId: String): ListWrapper<Int, Song>

    /**
     * Search
     */
    fun searchAll(query: String): Flow<List<LocalBaseItem>>
    fun searchSongs(query: String): Flow<List<Song>>
    fun searchArtists(query: String): Flow<List<Artist>>
    fun searchAlbums(query: String): Flow<List<Album>>
    fun searchPlaylists(query: String): Flow<List<Playlist>>

    /**
     * Song
     */
    suspend fun addSong(mediaMetadata: MediaMetadata)
    suspend fun safeAddSong(song: SongItem) = safeAddSongs(listOf(song))
    suspend fun safeAddSongs(songs: List<SongItem>): List<SongEntity>
    suspend fun refetchSongs(songs: List<Song>)
    suspend fun getSongById(songId: String): Song?
    fun getSongFile(songId: String): File
    fun hasSong(songId: String): DataWrapper<Boolean>
    suspend fun updateSongTitle(song: Song, newTitle: String)
    suspend fun setLiked(liked: Boolean, songs: List<Song>)
    suspend fun downloadSong(song: SongEntity) = downloadSongs(listOf(song))
    suspend fun downloadSongs(songs: List<SongEntity>)
    suspend fun onDownloadComplete(downloadId: Long, success: Boolean)
    suspend fun removeDownloads(songs: List<Song>)
    suspend fun moveToTrash(songs: List<Song>)
    suspend fun restoreFromTrash(songs: List<Song>)
    suspend fun deleteSongs(songs: List<Song>)

    /**
     * Artist
     */
    suspend fun getArtistById(artistId: String): ArtistEntity?
    suspend fun getArtistByName(name: String): ArtistEntity?
    suspend fun refetchArtist(artist: ArtistEntity)
    suspend fun updateArtist(artist: ArtistEntity)
    suspend fun deleteArtist(artist: ArtistEntity) = deleteArtists(listOf(artist))
    suspend fun deleteArtists(artists: List<ArtistEntity>)

    /**
     * Album
     */
    suspend fun addAlbum(album: AlbumItem) = addAlbums(listOf(album))
    suspend fun addAlbums(albums: List<AlbumItem>)
    suspend fun refetchAlbum(album: AlbumEntity)
    suspend fun deleteAlbums(albums: List<Album>)

    /**
     * Playlist
     */
    suspend fun insertPlaylist(playlist: PlaylistEntity)
    suspend fun addPlaylist(playlist: PlaylistItem) = addPlaylists(listOf(playlist))
    suspend fun addPlaylists(playlists: List<PlaylistItem>)
    suspend fun importPlaylist(playlist: PlaylistItem) = importPlaylists(listOf(playlist))
    suspend fun importPlaylists(playlists: List<PlaylistItem>)
    suspend fun addToPlaylist(playlist: PlaylistEntity, items: List<LocalItem>)
    suspend fun addToPlaylist(playlist: PlaylistEntity, item: YTItem)
    suspend fun addYouTubeItemsToPlaylist(playlist: PlaylistEntity, items: List<YTItem>)
    suspend fun refetchPlaylist(playlist: Playlist)
    suspend fun getPlaylistById(playlistId: String): Playlist
    suspend fun updatePlaylist(playlist: PlaylistEntity)
    suspend fun movePlaylistItems(playlistId: String, from: Int, to: Int)
    suspend fun removeSongFromPlaylist(playlistId: String, position: Int)
    suspend fun removeSongsFromPlaylist(playlistId: String, positions: List<Int>)
    suspend fun deletePlaylists(playlists: List<PlaylistEntity>)

    /**
     * Download
     */
    suspend fun addDownloadEntity(item: DownloadEntity)
    suspend fun getDownloadEntity(downloadId: Long): DownloadEntity?
    suspend fun removeDownloadEntity(downloadId: Long)

    /**
     * Search history
     */
    suspend fun getAllSearchHistory(): List<SearchHistory>
    suspend fun getSearchHistory(query: String): List<SearchHistory>
    suspend fun insertSearchHistory(query: String)
    suspend fun deleteSearchHistory(query: String)
}