package com.zionhuang.music.db

import android.content.Context
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingSource
import com.zionhuang.music.constants.SongSortType
import com.zionhuang.music.db.daos.ArtistDao
import com.zionhuang.music.db.daos.DownloadDao
import com.zionhuang.music.db.daos.PlaylistDao
import com.zionhuang.music.db.daos.SongDao
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.extensions.*
import com.zionhuang.music.utils.OkHttpDownloader
import com.zionhuang.music.utils.OkHttpDownloader.requestOf
import com.zionhuang.music.youtube.newpipe.ExtractorHelper
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

class SongRepository(private val context: Context) {
    private val musicDatabase = MusicDatabase.getInstance(context)
    private val songDao: SongDao = musicDatabase.songDao
    private val artistDao: ArtistDao = musicDatabase.artistDao
    private val playlistDao: PlaylistDao = musicDatabase.playlistDao
    private val downloadDao: DownloadDao = musicDatabase.downloadDao

    private val _deletedSongs = MutableLiveData<List<Song>>()
    val deletedSongs: LiveData<List<Song>> get() = _deletedSongs

    /**
     * All Songs
     */
    fun getAllSongsPagingSource(@SongSortType order: Int, descending: Boolean): PagingSource<Int, Song> =
        songDao.getAllSongsAsPagingSource(order, descending)

    fun searchSongs(query: String) = songDao.searchSongs(query)

    suspend fun getAllSongsList(@SongSortType order: Int, descending: Boolean): List<Song> =
        withContext(IO) { songDao.getAllSongsAsList(order, descending) }

    /**
     * Artist Songs
     */
    fun getArtistSongsAsPagingSource(artistId: Int, @SongSortType order: Int, descending: Boolean): PagingSource<Int, Song> =
        songDao.getArtistSongsAsPagingSource(artistId, order, descending)

    suspend fun getArtistSongsList(artistId: Int, @SongSortType order: Int, descending: Boolean) =
        withContext(IO) { songDao.getArtistSongsAsList(artistId, order, descending) }

    /**
     * Playlist Songs
     */
    fun getPlaylistSongsAsPagingSource(playlistId: Int): PagingSource<Int, Song> = songDao.getPlaylistSongsAsPagingSource(playlistId)

    suspend fun getPlaylistSongsList(playlistId: Int): List<Song> = withContext(IO) { songDao.getPlaylistSongsAsList(playlistId) }

    suspend fun getPlaylistSongEntities(playlistId: Int): List<PlaylistSongEntity> = withContext(IO) { playlistDao.getPlaylistSongEntities(playlistId) }

    /**
     * Artists
     */
    val allArtists: List<ArtistEntity> get() = artistDao.getAllArtists()
    val allArtistsPagingSource: PagingSource<Int, ArtistEntity> get() = artistDao.getAllArtistsAsPagingSource()

    /**
     * Playlists
     */
    val allPlaylistsPagingSource: PagingSource<Int, PlaylistEntity> get() = playlistDao.getAllPlaylistsAsPagingSource()

    /**
     * Song Operations
     */
    suspend fun getSongEntityById(id: String): SongEntity? = withContext(IO) { songDao.getSongEntityById(id) }

    suspend fun getSongById(id: String): Song? = withContext(IO) { songDao.getSongById(id) }

    private suspend fun artistSongsCount(artistId: Int) = withContext(IO) { songDao.artistSongsCount(artistId) }

    private suspend fun insert(song: SongEntity) = withContext(IO) { songDao.insert(song) }

    suspend fun insert(songs: List<Song>) = withContext(IO) {
        songs.forEach { song ->
            val stream = ExtractorHelper.getStreamInfo(song.songId)
            OkHttpDownloader.downloadFile(
                requestOf(stream.thumbnailUrl),
                context.getArtworkFile(song.songId)
            )
            if (song.duration == -1) song.duration = stream.duration.toInt()
            insert(song.toSongEntity())
        }
    }

    suspend fun insert(song: Song) = insert(listOf(song))

    private suspend fun updateSongEntity(song: SongEntity) =
        withContext(IO) { songDao.update(song) }

    private suspend fun updateSong(song: Song) = updateSongEntity(song.toSongEntity())

    /** [SongEntity.artistId] should not be edited. **/
    suspend fun updateSongEntity(songId: String, applier: SongEntity.() -> Unit) {
        getSongEntityById(songId)?.apply(applier)?.let { updateSongEntity(it) }
    }

    suspend fun updateSong(songId: String, applier: Song.() -> Unit) {
        val song = getSongById(songId) ?: return
        val artistName = song.artistName
        val newSong = song.apply(applier)
        updateSong(newSong)
        if (newSong.artistName != artistName && artistSongsCount(song.artistId) == 0) {
            deleteArtist(song.artistId)
        }
    }

    suspend fun toggleLike(songId: String) = updateSongEntity(songId) { liked = !liked }

    suspend fun deleteSongs(songs: List<Song>) = withContext(IO) {
        songDao.delete(songs.map { it.songId })
        songs.forEach { song ->
            context.getAudioFile(song.songId).takeIf { it.exists() }?.moveTo(context.getRecycledAudioFile(song.songId))
            context.getArtworkFile(song.songId).takeIf { it.exists() }?.moveTo(context.getRecycledArtworkFile(song.songId))
        }
        _deletedSongs.postValue(songs)
    }

    suspend fun restoreSongs(songs: List<Song>) {
        songs.forEach { song ->
            context.getRecycledAudioFile(song.songId).takeIf { it.exists() }?.moveTo(context.getAudioFile(song.songId))
            context.getRecycledArtworkFile(song.songId).takeIf { it.exists() }?.moveTo(context.getArtworkFile(song.songId))
        }
        insert(songs)
    }

    /**
     * Artist Operations
     */
    suspend fun getArtist(artistId: Int): ArtistEntity? = withContext(IO) { artistDao.getArtist(artistId) }

    suspend fun getArtist(name: String): Int? = withContext(IO) { artistDao.getArtistId(name) }

    private suspend fun getOrInsertArtist(name: String): Int = withContext(IO) { getArtist(name) ?: insertArtist(name) }

    @WorkerThread
    fun searchArtists(query: CharSequence): List<ArtistEntity> = artistDao.searchArtists(query.toString())

    private suspend fun insertArtist(name: String): Int = withContext(IO) { artistDao.insert(ArtistEntity(name = name)).toInt() }

    suspend fun updateArtist(artist: ArtistEntity) = withContext(IO) { artistDao.update(artist) }

    private suspend fun deleteArtist(artistId: Int) = withContext(IO) { artistDao.delete(artistId) }

    suspend fun deleteArtist(artist: ArtistEntity) = withContext(IO) { artistDao.delete(artist) }

    /**
     * Playlists
     */
    suspend fun getPlaylists() = withContext(IO) {
        playlistDao.getAllPlaylists()
    }

    suspend fun insertPlaylist(name: String) = withContext(IO) {
        playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    suspend fun updatePlaylist(playlist: PlaylistEntity) = withContext(IO) {
        playlistDao.updatePlaylist(playlist)
    }

    suspend fun updatePlaylistSongsOrder(playlistSongs: List<PlaylistSongEntity>) = withContext(IO) {
        Log.d(TAG, playlistSongs.toString())
        playlistDao.updatePlaylistSongs(playlistSongs)
    }

    suspend fun removeSongFromPlaylist(playlistId: Int, idInPlaylist: Int) = withContext(IO) {
        playlistDao.removeSong(playlistId, idInPlaylist)
    }

    suspend fun deletePlaylist(playlist: PlaylistEntity) = withContext(IO) {
        playlistDao.delete(playlist)
    }

    /**
     * Playlist songs
     */
    private suspend fun insertPlaylistSongs(playlistSongs: List<PlaylistSongEntity>) = withContext(IO) {
        playlistDao.insertPlaylistSongs(playlistSongs)
    }

    suspend fun addToPlaylist(songs: List<Song>, playlistId: Int) {
        var maxId = playlistDao.getPlaylistMaxId(playlistId) ?: -1
        insertPlaylistSongs(songs.map {
            PlaylistSongEntity(
                playlistId = playlistId,
                songId = it.songId,
                idInPlaylist = ++maxId
            )
        })
    }

    /**
     * Downloads
     */
    suspend fun getAllDownloads() = withContext(IO) {
        downloadDao.getAll()
    }

    suspend fun addDownload(downloadId: Long, songId: String) = withContext(IO) {
        downloadDao.insert(DownloadEntity(downloadId, songId))
    }

    suspend fun getSongIdByDownloadId(downloadId: Long): String = withContext(IO) {
        downloadDao.getDownloadEntity(downloadId).songId
    }

    suspend fun removeDownload(downloadId: Long) = withContext(IO) {
        downloadDao.delete(downloadDao.getDownloadEntity(downloadId))
    }

    /**
     * Extensions
     */
    private suspend fun Song.toSongEntity() = SongEntity(
        songId,
        title,
        getOrInsertArtist(artistName),
        duration,
        liked,
        artworkType,
        downloadState,
        createDate,
        modifyDate
    )
}