package com.zionhuang.music.repos

import android.app.DownloadManager
import android.util.Log
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import com.zionhuang.music.constants.MediaConstants.STATE_DOWNLOADED
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.db.daos.ArtistDao
import com.zionhuang.music.db.daos.DownloadDao
import com.zionhuang.music.db.daos.PlaylistDao
import com.zionhuang.music.db.daos.SongDao
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.extensions.*
import com.zionhuang.music.models.ListWrapper
import com.zionhuang.music.models.base.ISortInfo
import com.zionhuang.music.repos.base.LocalRepository
import com.zionhuang.music.repos.base.RemoteRepository
import com.zionhuang.music.utils.OkHttpDownloader
import com.zionhuang.music.youtube.NewPipeYouTubeHelper
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

object SongRepository : LocalRepository {
    private val context = getApplication()
    private val musicDatabase = MusicDatabase.getInstance(context)
    private val songDao: SongDao = musicDatabase.songDao
    private val artistDao: ArtistDao = musicDatabase.artistDao
    private val playlistDao: PlaylistDao = musicDatabase.playlistDao
    private val downloadDao: DownloadDao = musicDatabase.downloadDao
    private val remoteRepository: RemoteRepository = YouTubeRepository

    override suspend fun getSongById(songId: String): Song? = withContext(IO) { songDao.getSong(songId) }
    override fun searchSongs(query: String) = ListWrapper(
        getPagingSource = { songDao.searchSongsAsPagingSource(query) }
    )

    override suspend fun addSongs(songs: List<Song>) = songs.forEach {
        if (songDao.contains(it.id)) return@forEach
        try {
            val stream = NewPipeYouTubeHelper.getStreamInfo(it.id)
            OkHttpDownloader.downloadFile(
                OkHttpDownloader.requestOf(stream.thumbnailUrl),
                context.getArtworkFile(it.id)
            )
            songDao.insert(listOf(it.toSongEntity().copy(
                duration = if (it.duration == -1) stream.duration.toInt() else it.duration)
            ))
        } catch (e: Exception) {
            // TODO: Handle error
            Log.d(TAG, e.localizedMessage.orEmpty())
        }
    }

    override suspend fun updateSongs(songs: List<Song>) = withContext(IO) { songDao.update(songs.map { it.toSongEntity() }) }

    override suspend fun moveSongsToTrash(songs: List<Song>) {
        TODO("Not yet implemented")
    }

    override suspend fun restoreSongsFromTrash(songs: List<Song>) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteSongs(songs: List<Song>) = withContext(IO) { songDao.delete(songs.map { it.id }) }

    override suspend fun setLike(like: Boolean, songs: List<Song>) {
        TODO("Not yet implemented")
    }

    override suspend fun downloadSongs(songIds: List<String>) {
        songIds.forEach { id ->
            val song = getSongById(id) ?: return@forEach
            if (song.downloadState == STATE_DOWNLOADED) return@forEach
            val streamInfo = remoteRepository.getStream(id)
            OkHttpDownloader.downloadFile(
                OkHttpDownloader.requestOf(streamInfo.thumbnailUrl),
                context.getArtworkFile(id)
            )
            val downloadManager = context.getSystemService<DownloadManager>()!!
            val req = DownloadManager.Request(streamInfo.videoStreams.maxByOrNull { it.width }?.url?.toUri())
                .setTitle(streamInfo.name)
                .setDestinationUri((context.audioDir / streamInfo.id).toUri())
                .setVisibleInDownloadsUi(false)
            val did = downloadManager.enqueue(req)
            addDownload(DownloadEntity(did, id))
        }
    }


    override fun getAllSongs(sortInfo: ISortInfo): ListWrapper<Int, Song> = ListWrapper<Int, Song>(
        getList = { withContext(IO) { songDao.getAllSongsAsList(sortInfo) } },
        getPagingSource = { songDao.getAllSongsAsPagingSource(sortInfo) }
    )

    override fun getArtistSongs(artistId: Int, sortInfo: ISortInfo): ListWrapper<Int, Song> = ListWrapper(
        getList = { withContext(IO) { songDao.getArtistSongsAsList(artistId, sortInfo) } },
        getPagingSource = { songDao.getArtistSongsAsPagingSource(artistId, sortInfo) }
    )

    override fun getPlaylistSongs(playlistId: Int, sortInfo: ISortInfo): ListWrapper<Int, Song> = ListWrapper(
        getList = { withContext(IO) { songDao.getPlaylistSongsAsList(playlistId) } },
        getPagingSource = { songDao.getPlaylistSongsAsPagingSource(playlistId) }
    )


    override fun getAllArtists() = ListWrapper(
        getList = { withContext(IO) { artistDao.getAllArtistsAsList() } },
        getPagingSource = { artistDao.getAllArtistsAsPagingSource() }
    )

    override suspend fun getArtistById(artistId: Int): ArtistEntity? = withContext(IO) { artistDao.getArtist(artistId) }
    override fun searchArtists(query: String) = ListWrapper<Int, ArtistEntity>(
        getList = { withContext(IO) { artistDao.searchArtists(query) } }
    )

    override suspend fun addArtist(artist: ArtistEntity) {
        withContext(IO) {
            artistDao.insert(artist)
        }
    }

    override suspend fun updateArtist(artist: ArtistEntity) = withContext(IO) { artistDao.update(artist) }
    override suspend fun deleteArtist(artist: ArtistEntity) = withContext(IO) { artistDao.delete(artist) }


    override fun getAllPlaylists() = ListWrapper(
        getList = { withContext(IO) { playlistDao.getAllPlaylistsAsList() } },
        getPagingSource = { playlistDao.getAllPlaylistsAsPagingSource() }
    )

    override suspend fun getPlaylistById(playlistId: Int): PlaylistEntity? = withContext(IO) { playlistDao.getPlaylist(playlistId) }
    override fun searchPlaylists(query: String) = ListWrapper<Int, PlaylistEntity>(
        getList = { withContext(IO) { playlistDao.searchPlaylists(query) } }
    )

    override suspend fun addPlaylist(playlist: PlaylistEntity) = withContext(IO) { playlistDao.insertPlaylist(playlist) }
    override suspend fun updatePlaylist(playlist: PlaylistEntity) = withContext(IO) { playlistDao.updatePlaylist(playlist) }
    override suspend fun deletePlaylist(playlist: PlaylistEntity) = withContext(IO) { playlistDao.deletePlaylist(playlist) }


    override suspend fun getPlaylistSongEntities(playlistId: Int) = ListWrapper<Int, PlaylistSongEntity>(
        getList = { withContext(IO) { playlistDao.getPlaylistSongEntities(playlistId) } }
    )

    override suspend fun updatePlaylistSongEntities(playlistSongEntities: List<PlaylistSongEntity>) = withContext(IO) { playlistDao.updatePlaylistSongEntities(playlistSongEntities) }

    override suspend fun addSongsToPlaylist(playlistId: Int, songs: List<Song>) {
        var maxId = playlistDao.getPlaylistMaxId(playlistId) ?: -1
        playlistDao.insertPlaylistSongEntities(songs.map {
            PlaylistSongEntity(
                playlistId = playlistId,
                songId = it.id,
                idInPlaylist = ++maxId
            )
        })
    }

    override suspend fun removeSongsFromPlaylist(playlistId: Int, idInPlaylist: List<Int>) = withContext(IO) { playlistDao.deletePlaylistSongEntities(playlistId, idInPlaylist) }


    override fun getAllDownloads() = ListWrapper<Int, DownloadEntity>(
        getFlow = { downloadDao.getAllDownloadEntities() }
    )

    override suspend fun getDownloadEntity(downloadId: Long): DownloadEntity? = withContext(IO) { downloadDao.getDownloadEntity(downloadId) }
    override suspend fun addDownload(item: DownloadEntity) = withContext(IO) { downloadDao.insert(item) }
    override suspend fun removeDownload(downloadId: Long) = withContext(IO) { downloadDao.delete(downloadId) }


    private suspend fun Song.toSongEntity() = SongEntity(
        id,
        title,
        withContext(IO) { artistDao.getArtistId(artistName) ?: artistDao.insert(ArtistEntity(name = artistName)).toInt() },
        duration,
        liked,
        artworkType,
        downloadState,
        createDate,
        modifyDate
    )
}