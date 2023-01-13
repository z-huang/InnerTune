package com.zionhuang.music.repos.base

import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.innertube.models.PlaylistItem
import com.zionhuang.innertube.models.YTItem
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.models.sortInfo.*
import kotlinx.coroutines.flow.Flow
import java.io.File

interface LocalRepository {
    fun getAllSongId(): Flow<List<String>>
    fun getAllLikedSongId(): Flow<List<String>>
    fun getAllAlbumId(): Flow<List<String>>
    fun getAllPlaylistId(): Flow<List<String>>

    /**
     * Browse
     */
    fun getAllSongs(sortInfo: ISortInfo<SongSortType>): Flow<List<Song>>
    suspend fun getSongCount(): Int

    fun getAllArtists(sortInfo: ISortInfo<ArtistSortType>): Flow<List<Artist>>
    suspend fun getArtistCount(): Int

    suspend fun getArtistSongsPreview(artistId: String): Flow<List<Song>>
    fun getArtistSongs(artistId: String, sortInfo: ISortInfo<SongSortType>): Flow<List<Song>>
    suspend fun getArtistSongCount(artistId: String): Int

    fun getAllAlbums(sortInfo: ISortInfo<AlbumSortType>): Flow<List<Album>>
    suspend fun getAlbumCount(): Int
    suspend fun getAlbumSongs(albumId: String): List<Song>

    fun getAllPlaylists(sortInfo: ISortInfo<PlaylistSortType>): Flow<List<Playlist>>

    fun getPlaylistSongs(playlistId: String): Flow<List<Song>>

    fun getLikedSongs(sortInfo: ISortInfo<SongSortType>): Flow<List<Song>>
    fun getLikedSongCount(): Flow<Int>
    fun getDownloadedSongs(sortInfo: ISortInfo<SongSortType>): Flow<List<Song>>
    fun getDownloadedSongCount(): Flow<Int>

    /**
     * Search
     */
    fun searchAll(query: String): Flow<List<LocalItem>>
    fun searchSongs(query: String): Flow<List<Song>>
    fun searchDownloadedSongs(query: String): Flow<List<Song>>
    fun searchArtists(query: String): Flow<List<Artist>>
    fun searchAlbums(query: String): Flow<List<Album>>
    fun searchPlaylists(query: String): Flow<List<Playlist>>

    /**
     * Song
     */
    suspend fun addSong(mediaMetadata: MediaMetadata) = addSongs(listOf(mediaMetadata))
    suspend fun addSongs(songs: List<MediaMetadata>)
    suspend fun refetchSong(song: Song) = refetchSongs(listOf(song))
    suspend fun refetchSongs(songs: List<Song>)
    fun getSongById(songId: String?): Flow<Song?>
    fun getSongFile(songId: String): File
    suspend fun incrementSongTotalPlayTime(songId: String, playTime: Long)
    suspend fun updateSongDuration(songId: String, duration: Int)
    suspend fun updateSongTitle(song: Song, newTitle: String)
    suspend fun toggleLiked(song: Song) = toggleLiked(listOf(song))
    suspend fun toggleLiked(songs: List<Song>)
    suspend fun downloadSong(songId: String) = downloadSongs(listOf(songId))
    suspend fun downloadSongs(songIds: List<String>)
    suspend fun onDownloadComplete(downloadId: Long, success: Boolean)
    suspend fun validateDownloads()
    suspend fun removeDownloads(songs: List<Song>)
    suspend fun moveToTrash(songs: List<Song>)
    suspend fun restoreFromTrash(songs: List<Song>)
    suspend fun deleteSong(songId: String)
    suspend fun deleteSong(song: Song) = deleteSongs(listOf(song))
    suspend fun deleteSongs(songs: List<Song>)

    /**
     * Artist
     */
    suspend fun getArtistById(artistId: String): ArtistEntity?
    suspend fun getArtistByName(name: String): ArtistEntity?
    suspend fun refetchArtist(artist: ArtistEntity) = refetchArtists(listOf(artist))
    suspend fun refetchArtists(artists: List<ArtistEntity>)
    suspend fun updateArtist(artist: ArtistEntity)

    /**
     * Album
     */
    suspend fun addAlbum(album: AlbumItem) = addAlbums(listOf(album))
    suspend fun addAlbums(albums: List<AlbumItem>)
    suspend fun getAlbum(albumId: String): Album?
    suspend fun getAlbumWithSongs(albumId: String): AlbumWithSongs?
    suspend fun deleteAlbum(albumId: String)
    suspend fun deleteAlbums(albums: List<Album>)

    /**
     * Playlist
     */
    suspend fun addPlaylist(playlist: PlaylistItem) = addPlaylists(listOf(playlist))
    suspend fun addPlaylists(playlists: List<PlaylistItem>)
    suspend fun importPlaylist(playlist: PlaylistItem) = importPlaylists(listOf(playlist))
    suspend fun importPlaylists(playlists: List<PlaylistItem>)
    suspend fun insertPlaylist(playlist: PlaylistEntity)
    fun getPlaylist(playlistId: String): Flow<Playlist>
    suspend fun addToPlaylist(playlist: PlaylistEntity, item: LocalItem) = addToPlaylist(playlist, listOf(item))
    suspend fun addToPlaylist(playlist: PlaylistEntity, items: List<LocalItem>)
    suspend fun addYouTubeItemToPlaylist(playlist: PlaylistEntity, item: YTItem) = addYTItemsToPlaylist(playlist, listOf(item))
    suspend fun addYTItemsToPlaylist(playlist: PlaylistEntity, items: List<YTItem>)
    suspend fun addMediaMetadataToPlaylist(playlist: PlaylistEntity, mediaMetadata: MediaMetadata)
    suspend fun refetchPlaylists(playlists: List<Playlist>)
    suspend fun downloadPlaylists(playlists: List<Playlist>)
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
    fun getAllSearchHistory(): Flow<List<SearchHistory>>
    fun getSearchHistory(query: String): Flow<List<SearchHistory>>
    suspend fun insertSearchHistory(query: String)
    suspend fun deleteSearchHistory(query: String)
    suspend fun clearSearchHistory()

    /**
     * Format
     */
    fun getSongFormat(songId: String?): Flow<FormatEntity?>
    suspend fun upsert(format: FormatEntity)

    /**
     * Lyrics
     */
    fun getLyrics(songId: String?): Flow<LyricsEntity?>
    suspend fun hasLyrics(songId: String): Boolean
    suspend fun upsert(lyrics: LyricsEntity)
    suspend fun deleteLyrics(songId: String)
}
