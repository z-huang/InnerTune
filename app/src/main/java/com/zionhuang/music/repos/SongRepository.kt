package com.zionhuang.music.repos

import android.app.DownloadManager
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.distinctUntilChanged
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.YouTube.MAX_GET_QUEUE_SIZE
import com.zionhuang.innertube.models.*
import com.zionhuang.music.R
import com.zionhuang.music.constants.MediaConstants.STATE_DOWNLOADED
import com.zionhuang.music.constants.MediaConstants.STATE_DOWNLOADING
import com.zionhuang.music.constants.MediaConstants.STATE_NOT_DOWNLOADED
import com.zionhuang.music.constants.MediaConstants.STATE_PREPARING
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.db.daos.DownloadDao
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.db.entities.ArtistEntity.Companion.generateArtistId
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.generatePlaylistId
import com.zionhuang.music.extensions.*
import com.zionhuang.music.models.DataWrapper
import com.zionhuang.music.models.ListWrapper
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.models.base.ISortInfo
import com.zionhuang.music.repos.base.LocalRepository
import com.zionhuang.music.ui.bindings.resizeThumbnailUrl
import com.zionhuang.music.utils.md5
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.io.File

object SongRepository : LocalRepository {
    private val context = getApplication()
    private val database = MusicDatabase.getInstance(context)
    private val songDao = database.songDao
    private val artistDao = database.artistDao
    private val albumDao = database.albumDao
    private val playlistDao = database.playlistDao
    private val downloadDao: DownloadDao = database.downloadDao

    private var autoDownload by context.preference(R.string.pref_auto_download, false)

    override suspend fun getSongById(songId: String): Song? = withContext(IO) { songDao.getSong(songId) }

    override fun searchSongs(query: String) = ListWrapper(
        getPagingSource = { songDao.searchSongsAsPagingSource(query) }
    )

    override fun hasSong(songId: String): DataWrapper<Boolean> = DataWrapper(
        getValueAsync = { songDao.hasSong(songId) },
        getLiveData = { songDao.hasSongLiveData(songId).distinctUntilChanged() }
    )

    suspend fun addSong(mediaMetadata: MediaMetadata) = withContext(IO) {
        songDao.insert(mediaMetadata.toSongEntity())
        mediaMetadata.artists.forEachIndexed { index, artist ->
            val artistId = getArtistByName(artist.name)?.id ?: artist.id.also {
                artistDao.insert(ArtistEntity(
                    id = artist.id,
                    name = artist.name
                ))
            }
            artistDao.insert(SongArtistMap(
                songId = mediaMetadata.id,
                artistId = artistId,
                position = index
            ))
        }
        if (autoDownload) {
            getSongById(mediaMetadata.id)?.let { downloadSong(it) }
        }
    }

    /**
     * Safe add song: call [YouTube.getQueue] to ensure we get full information
     */
    suspend fun safeAddSong(song: SongItem) = safeAddSongs(listOf(song))
    suspend fun safeAddSongs(songs: List<SongItem>) = withContext(IO) {
        songs.chunked(MAX_GET_QUEUE_SIZE).forEach { chunk ->
            addSongs(YouTube.getQueue(chunk.map { it.id }))
        }
    }

    suspend fun addSongs(songs: List<SongItem>) = withContext(IO) {
        songs.forEach { song ->
            songDao.insert(song.toSongEntity())
            song.artists.forEachIndexed { index, run ->
                // for artists that can't browse or have no id, we treat them as local artists
                val artistId = (getArtistByName(run.text)?.id ?: run.navigationEndpoint?.browseEndpoint?.browseId ?: generateArtistId()).also {
                    artistDao.insert(ArtistEntity(
                        id = it,
                        name = run.text
                    ))
                }
                artistDao.insert(SongArtistMap(
                    songId = song.id,
                    artistId = artistId,
                    position = index
                ))
            }
            if (autoDownload) {
                getSongById(song.id)?.let { downloadSong(it) }
            }
        }
    }

    suspend fun addAlbum(album: AlbumItem) = addAlbums(listOf(album))
    suspend fun addAlbums(albums: List<AlbumItem>) = withContext(IO) {
        albums.forEach { album ->
            YouTube.getQueue(playlistId = album.playlistId).let { songs ->
                albumDao.insert(AlbumEntity(
                    id = album.id,
                    title = album.title,
                    year = album.year,
                    thumbnailUrl = album.thumbnails.last().url,
                    songCount = songs.size,
                    duration = songs.sumOf { it.duration ?: 0 }
                ))
                addSongs(songs)
                albumDao.upsert(songs.mapIndexed { index, songItem ->
                    SongAlbumMap(
                        songId = songItem.id,
                        albumId = album.id,
                        index = index
                    )
                })
            }
            (YouTube.browse(BrowseEndpoint(browseId = album.id)).items.firstOrNull() as? AlbumOrPlaylistHeader)?.artists?.forEachIndexed { index, run ->
                val artistId = (getArtistByName(run.text)?.id ?: run.navigationEndpoint?.browseEndpoint?.browseId ?: generateArtistId()).also {
                    artistDao.insert(ArtistEntity(
                        id = it,
                        name = run.text
                    ))
                }
                albumDao.insert(AlbumArtistMap(
                    albumId = album.id,
                    artistId = artistId,
                    order = index
                ))
            }
        }
    }

    suspend fun addPlaylist(playlist: PlaylistItem) = withContext(IO) {
        (YouTube.browse(BrowseEndpoint(browseId = "VL" + playlist.id)).items.firstOrNull() as? AlbumOrPlaylistHeader)?.let { header ->
            playlistDao.insert(PlaylistEntity(
                id = playlist.id,
                name = header.name,
                author = header.artists?.firstOrNull()?.text,
                authorId = header.artists?.firstOrNull()?.navigationEndpoint?.browseEndpoint?.browseId,
                year = header.year,
                thumbnailUrl = header.thumbnails.lastOrNull()?.url
            ))
        }
    }

    suspend fun importPlaylist(playlist: PlaylistItem) = importPlaylists(listOf(playlist))
    suspend fun importPlaylists(playlists: List<PlaylistItem>) = withContext(IO) {
        playlists.forEach { playlist ->
            val playlistId = generatePlaylistId()
            playlistDao.insert(playlist.toPlaylistEntity().copy(id = playlistId))
            var index = 0
            var browseResult: BrowseResult? = null
            do {
                browseResult = if (browseResult == null) {
                    YouTube.browse(BrowseEndpoint(browseId = "VL" + playlist.id))
                } else {
                    YouTube.browse(browseResult.continuations!!)
                }
                browseResult.items.filterIsInstance<SongItem>().let { items ->
                    safeAddSongs(items)
                    playlistDao.insert(items.map {
                        PlaylistSongMap(
                            playlistId = playlistId,
                            songId = it.id,
                            idInPlaylist = index++
                        )
                    })
                }
            } while (!browseResult?.continuations.isNullOrEmpty())
        }
    }

    suspend fun updateSongTitle(song: Song, newTitle: String) = withContext(IO) {
        songDao.update(song.song.copy(title = newTitle))
    }

    override suspend fun moveToTrash(songs: List<Song>) = withContext(IO) {
        songDao.update(songs.map { it.song.copy(isTrash = true) })
    }

    override suspend fun restoreFromTrash(songs: List<Song>) = withContext(IO) {
        songDao.update(songs.map { it.song.copy(isTrash = false) })
    }

    override suspend fun deleteSongs(songs: List<Song>) = withContext(IO) {
        songDao.delete(songs.map { it.song })
        songs.forEach { song ->
            getSongFile(song.song.id).delete()
            song.artists.forEach { artist ->
                if (artistDao.getArtistSongCount(artist.id) == 0) {
                    deleteArtist(artist)
                }
            }
        }
    }

    override suspend fun setLiked(liked: Boolean, songs: List<Song>) = withContext(IO) {
        songDao.update(songs.map { it.song.copy(liked = liked) })
    }

    override suspend fun downloadSongs(songs: List<Song>) = withContext(IO) {
        songs.filter { it.song.downloadState == STATE_NOT_DOWNLOADED }.let { songs ->
            songDao.update(songs.map { it.song.copy(downloadState = STATE_PREPARING) })
            songs.forEach { song ->
                val playerResponse = YouTube.player(videoId = song.song.id)
                if (playerResponse.playabilityStatus.status == "OK") {
                    val url = playerResponse.streamingData?.adaptiveFormats
                        ?.filter { it.isAudio }
                        ?.maxByOrNull { it.bitrate }
                        ?.url
                    if (url == null) {
                        songDao.update(song.song.copy(downloadState = STATE_NOT_DOWNLOADED))
                        // TODO
                    } else {
                        songDao.update(song.song.copy(downloadState = STATE_DOWNLOADING))
                        val downloadManager = context.getSystemService<DownloadManager>()!!
                        val req = DownloadManager.Request(url.toUri())
                            .setTitle(song.song.title)
                            .setDestinationUri(getSongFile(song.song.id).toUri())
                            .setVisibleInDownloadsUi(false)
                        val did = downloadManager.enqueue(req)
                        addDownload(DownloadEntity(did, song.song.id))
                    }
                } else {
                    songDao.update(song.song.copy(downloadState = STATE_NOT_DOWNLOADED))
                    // TODO
                }
            }
        }
    }

    suspend fun onDownloadComplete(downloadId: Long, success: Boolean) = withContext(IO) {
        getDownloadEntity(downloadId)?.songId?.let { songId ->
            getSongById(songId)?.let { song ->
                songDao.update(song.song.copy(downloadState = if (success) STATE_DOWNLOADED else STATE_NOT_DOWNLOADED))
                removeDownloadEntity(downloadId)
            }
        }
    }

    override suspend fun removeDownloads(songs: List<Song>) = withContext(IO) {
        songs.forEach { song ->
            if (song.song.downloadState == STATE_DOWNLOADED) {
                if (!getSongFile(song.song.id).exists() || getSongFile(song.song.id).delete()) {
                    songDao.update(song.song.copy(downloadState = STATE_NOT_DOWNLOADED))
                }
            }
        }
    }

    override fun getSongFile(songId: String): File {
        val mediaDir = context.getExternalFilesDir(null)!! / "media"
        if (!mediaDir.isDirectory) mediaDir.mkdirs()
        return mediaDir / md5(songId)
    }

    override fun getSongArtworkFile(songId: String): File {
        val artworkDir = context.getExternalFilesDir(null)!! / "artwork"
        if (!artworkDir.isDirectory) artworkDir.mkdirs()
        return artworkDir / md5(songId)
    }


    override fun getAllSongs(sortInfo: ISortInfo): ListWrapper<Int, Song> = ListWrapper<Int, Song>(
        getList = { withContext(IO) { songDao.getAllSongsAsList(sortInfo) } },
        getPagingSource = { songDao.getAllSongsAsPagingSource(sortInfo) }
    )

    override fun getArtistSongs(artistId: String, sortInfo: ISortInfo): ListWrapper<Int, Song> = ListWrapper(
        getList = { withContext(IO) { songDao.getArtistSongsAsList(artistId, sortInfo) } },
        getPagingSource = { songDao.getArtistSongsAsPagingSource(artistId, sortInfo) }
    )

    override fun getPlaylistSongs(playlistId: String, sortInfo: ISortInfo): ListWrapper<Int, Song> = ListWrapper(
        getList = { withContext(IO) { songDao.getPlaylistSongsAsList(playlistId) } },
        getPagingSource = { songDao.getPlaylistSongsAsPagingSource(playlistId) }
    )


    override fun getAllArtists() = ListWrapper(
        getPagingSource = { artistDao.getAllArtistsAsPagingSource() }
    )

    override suspend fun getArtistById(artistId: String): ArtistEntity? = withContext(IO) {
        artistDao.getArtistById(artistId)
    }

    override suspend fun getArtistByName(name: String): ArtistEntity? = withContext(IO) {
        artistDao.getArtistByName(name)
    }

    override fun searchArtists(query: String) = ListWrapper<Int, ArtistEntity>(
        getList = { withContext(IO) { artistDao.searchArtists(query) } }
    )

    override suspend fun addArtist(artist: ArtistEntity): Unit = withContext(IO) {
        artistDao.insert(artist)
    }


    suspend fun refetchArtist(artist: ArtistEntity) = withContext(IO) {
        if (artist.isYouTubeArtist) {
            val browseResult = YouTube.browse(BrowseEndpoint(browseId = artist.id))
            val header = browseResult.items.firstOrNull()
            if (header is ArtistHeader) {
                artistDao.update(ArtistEntity(
                    id = artist.id,
                    name = header.name,
                    thumbnailUrl = resizeThumbnailUrl(header.bannerThumbnails.lastOrNull()?.url, 400, 400),
                    bannerUrl = header.bannerThumbnails.lastOrNull()?.url,
                    description = header.description,
                ))
            }
        }
    }

    override suspend fun updateArtist(artist: ArtistEntity) = withContext(IO) {
        artistDao.update(artist)
    }

    override suspend fun deleteArtist(artist: ArtistEntity) = withContext(IO) {
        artistDao.delete(artist)
    }

    override fun getAllAlbums() = ListWrapper(
        getPagingSource = { albumDao.getAllAlbumsAsPagingSource() }
    )

    suspend fun fetchAlbumThumbnail(album: AlbumEntity) = withContext(IO) {
        (YouTube.browse(BrowseEndpoint(browseId = album.id)).items.firstOrNull() as? AlbumOrPlaylistHeader)?.let { header ->
            albumDao.update(album.copy(thumbnailUrl = header.thumbnails.lastOrNull()?.url))
        }
    }

    override fun getAllPlaylists() = ListWrapper(
        getList = { withContext(IO) { playlistDao.getAllPlaylistsAsList() } },
        getPagingSource = { playlistDao.getAllPlaylistsAsPagingSource() }
    )

    override suspend fun getPlaylistById(playlistId: String): PlaylistEntity? = withContext(IO) {
        playlistDao.getPlaylist(playlistId)
    }

    override fun searchPlaylists(query: String) = ListWrapper<Int, PlaylistEntity>(
        getList = { withContext(IO) { playlistDao.searchPlaylists(query) } }
    )

    override suspend fun addPlaylist(playlist: PlaylistEntity) = withContext(IO) { playlistDao.insert(playlist) }
    override suspend fun updatePlaylist(playlist: PlaylistEntity) = withContext(IO) { playlistDao.update(playlist) }
    override suspend fun deletePlaylist(playlist: PlaylistEntity) = withContext(IO) { playlistDao.delete(playlist) }


    override suspend fun getPlaylistSongEntities(playlistId: String) = ListWrapper<Int, PlaylistSongMap>(
        getList = { withContext(IO) { playlistDao.getPlaylistSongEntities(playlistId) } }
    )

    override suspend fun updatePlaylistSongEntities(playlistSongEntities: List<PlaylistSongMap>) = withContext(IO) { playlistDao.updatePlaylistSongEntities(playlistSongEntities) }

    override suspend fun addSongsToPlaylist(playlistId: String, songs: List<Song>) {
        var maxId = playlistDao.getPlaylistMaxId(playlistId) ?: -1
        playlistDao.insertPlaylistSongEntities(songs.map {
            PlaylistSongMap(
                playlistId = playlistId,
                songId = it.song.id,
                idInPlaylist = ++maxId
            )
        })
    }

    override suspend fun removeSongsFromPlaylist(playlistId: String, idInPlaylist: List<Int>) = withContext(IO) { playlistDao.deletePlaylistSongEntities(playlistId, idInPlaylist) }

    override fun getAllDownloads() = ListWrapper<Int, DownloadEntity>(
        getLiveData = { downloadDao.getAllDownloadEntitiesAsLiveData() }
    )

    override suspend fun getDownloadEntity(downloadId: Long): DownloadEntity? = withContext(IO) { downloadDao.getDownloadEntity(downloadId) }
    override suspend fun addDownload(item: DownloadEntity) = withContext(IO) { downloadDao.insert(item) }
    override suspend fun removeDownloadEntity(downloadId: Long) = withContext(IO) { downloadDao.delete(downloadId) }

    override suspend fun getAllSearchHistory() = withContext(IO) {
        database.searchHistoryDao.getAllHistory()
    }

    override suspend fun getSearchHistory(query: String) = withContext(IO) {
        database.searchHistoryDao.getHistory(query)
    }

    override suspend fun insertSearchHistory(query: String) = withContext(IO) {
        database.searchHistoryDao.insert(SearchHistory(query = query))
    }

    override suspend fun deleteSearchHistory(query: String) = withContext(IO) {
        database.searchHistoryDao.delete(query)
    }
}