package com.zionhuang.music.repos

import android.app.DownloadManager
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.lifecycle.distinctUntilChanged
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.YouTube.MAX_GET_QUEUE_SIZE
import com.zionhuang.innertube.models.*
import com.zionhuang.innertube.models.ArtistHeader
import com.zionhuang.innertube.utils.browseAll
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
import com.zionhuang.music.models.sortInfo.*
import com.zionhuang.music.repos.base.LocalRepository
import com.zionhuang.music.ui.bindings.resizeThumbnailUrl
import com.zionhuang.music.utils.md5
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.map
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
        getLiveData = { songDao.hasSongAsLiveData(songId).distinctUntilChanged() }
    )

    suspend fun addSong(mediaMetadata: MediaMetadata) = withContext(IO) {
        val song = mediaMetadata.toSongEntity()
        songDao.insert(song)
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
        if (autoDownload) downloadSong(song)
    }

    /**
     * Safe add song: call [YouTube.getQueue] to ensure we get full information
     */
    suspend fun safeAddSong(song: SongItem) = safeAddSongs(listOf(song))
    suspend fun safeAddSongs(songs: List<SongItem>): List<SongEntity> = withContext(IO) {
        songs.chunked(MAX_GET_QUEUE_SIZE).flatMap { chunk ->
            addSongs(YouTube.getQueue(chunk.map { it.id }))
        }
    }

    suspend fun addSong(song: SongItem) = addSongs(listOf(song))
    suspend fun addSongs(items: List<SongItem>) = withContext(IO) {
        val songs = items.map { it.toSongEntity() }
        val songArtistMaps = items.flatMap { song ->
            song.artists.mapIndexed { index, run ->
                val artistId = getArtistByName(run.text)?.id ?: (run.navigationEndpoint?.browseEndpoint?.browseId ?: generateArtistId()).also {
                    artistDao.insert(ArtistEntity(
                        id = it,
                        name = run.text
                    ))
                }
                SongArtistMap(
                    songId = song.id,
                    artistId = artistId,
                    position = index
                )
            }
        }
        songDao.insert(songs)
        artistDao.insert(songArtistMaps)
        if (autoDownload) downloadSongs(songs)
        return@withContext songs
    }

    suspend fun addAlbum(album: AlbumItem) = addAlbums(listOf(album))
    suspend fun addAlbums(albums: List<AlbumItem>) = withContext(IO) {
        albums.forEach { album ->
            val ids = YouTube.browse(BrowseEndpoint(browseId = "VL" + album.playlistId)).items.filterIsInstance<SongItem>().map { it.id }
            YouTube.getQueue(videoIds = ids).let { songs ->
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
            playlistDao.insert(header.toPlaylistEntity())
        }
    }

    suspend fun importPlaylist(playlist: PlaylistItem) = importPlaylists(listOf(playlist))
    suspend fun importPlaylists(playlists: List<PlaylistItem>) = withContext(IO) {
        playlists.forEach { playlist ->
            val playlistId = generatePlaylistId()
            playlistDao.insert(playlist.toPlaylistEntity().copy(id = playlistId))
            var index = 0
            val songs = YouTube.browseAll(BrowseEndpoint(browseId = "VL" + playlist.id)).filterIsInstance<SongItem>()
            safeAddSongs(songs)
            playlistDao.insert(songs.map {
                PlaylistSongMap(
                    playlistId = playlistId,
                    songId = it.id,
                    idInPlaylist = index++
                )
            })
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

    override suspend fun downloadSongs(songs: List<SongEntity>) = withContext(IO) {
        songs.filter { it.downloadState == STATE_NOT_DOWNLOADED }.let { songs ->
            songDao.update(songs.map { it.copy(downloadState = STATE_PREPARING) })
            songs.forEach { song ->
                val playerResponse = YouTube.player(videoId = song.id)
                if (playerResponse.playabilityStatus.status == "OK") {
                    val url = playerResponse.streamingData?.adaptiveFormats
                        ?.filter { it.isAudio }
                        ?.maxByOrNull { it.bitrate }
                        ?.url
                    if (url == null) {
                        songDao.update(song.copy(downloadState = STATE_NOT_DOWNLOADED))
                        // TODO
                    } else {
                        songDao.update(song.copy(downloadState = STATE_DOWNLOADING))
                        val downloadManager = context.getSystemService<DownloadManager>()!!
                        val req = DownloadManager.Request(url.toUri())
                            .setTitle(song.title)
                            .setDestinationUri(getSongFile(song.id).toUri())
                        val did = downloadManager.enqueue(req)
                        addDownload(DownloadEntity(did, song.id))
                    }
                } else {
                    songDao.update(song.copy(downloadState = STATE_NOT_DOWNLOADED))
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


    override fun getAllSongs(sortInfo: ISortInfo<SongSortType>): ListWrapper<Int, Song> = ListWrapper(
        getList = { withContext(IO) { songDao.getAllSongsAsList(sortInfo) } },
        getFlow = {
            if (sortInfo.type != SongSortType.ARTIST) {
                songDao.getAllSongsAsFlow(sortInfo)
            } else {
                songDao.getAllSongsAsFlow(SortInfo(SongSortType.CREATE_DATE, true)).map { list ->
                    list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { song ->
                        song.artists.joinToString(separator = "") { it.name }
                    }).reversed(sortInfo.isDescending)
                }
            }
        },
        getPagingSource = { songDao.getAllSongsAsPagingSource(sortInfo) }
    )

    suspend fun getSongCount() = withContext(IO) { songDao.getSongCount() }

    override fun getArtistSongs(artistId: String, sortInfo: ISortInfo<SongSortType>): ListWrapper<Int, Song> = ListWrapper(
        getList = { withContext(IO) { songDao.getArtistSongsAsList(artistId, sortInfo) } },
        getFlow = {
            songDao.getArtistSongsAsFlow(artistId, if (sortInfo.type == SongSortType.ARTIST) SortInfo(SongSortType.CREATE_DATE, sortInfo.isDescending) else sortInfo)
        }
    )

    suspend fun getArtistSongCount(artistId: String) = withContext(IO) { songDao.getArtistSongCount(artistId) }

    override fun getPlaylistSongs(playlistId: String, sortInfo: ISortInfo<SongSortType>): ListWrapper<Int, Song> = ListWrapper(
        getList = { withContext(IO) { songDao.getPlaylistSongsAsList(playlistId) } },
        getPagingSource = { songDao.getPlaylistSongsAsPagingSource(playlistId) }
    )

    override fun getAllArtists(sortInfo: ISortInfo<ArtistSortType>) = ListWrapper<Int, Artist>(
        getFlow = {
            if (sortInfo.type != ArtistSortType.SONG_COUNT) {
                artistDao.getAllArtistsAsFlow(sortInfo)
            } else {
                artistDao.getAllArtistsAsFlow(SortInfo(ArtistSortType.CREATE_DATE, true)).map { list ->
                    list.sortedBy { it.songCount }.reversed(sortInfo.isDescending)
                }
            }
        }
    )

    override suspend fun getArtistById(artistId: String): ArtistEntity? = withContext(IO) {
        artistDao.getArtistById(artistId)
    }

    override suspend fun getArtistByName(name: String): ArtistEntity? = withContext(IO) {
        artistDao.getArtistByName(name)
    }

    suspend fun getArtistCount() = withContext(IO) { artistDao.getArtistCount() }

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

    override suspend fun deleteArtists(artists: List<ArtistEntity>) = withContext(IO) {
        artistDao.delete(artists)
    }

    override fun getAllAlbums(sortInfo: ISortInfo<AlbumSortType>) = ListWrapper<Int, Album>(
        getFlow = {
            if (sortInfo.type == AlbumSortType.ARTIST) {
                albumDao.getAllAlbumsAsFlow(SortInfo(AlbumSortType.CREATE_DATE, true)).map { list ->
                    list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { song ->
                        song.artists.joinToString(separator = "") { it.name }
                    }).reversed(sortInfo.isDescending)
                }
            } else {
                albumDao.getAllAlbumsAsFlow(sortInfo)
            }
        }
    )

    suspend fun getAlbumSongs(albumId: String) = withContext(IO) {
        songDao.getAlbumSongs(albumId)
    }

    suspend fun getAlbumCount() = withContext(IO) { albumDao.getAlbumCount() }

    suspend fun refetchAlbum(album: AlbumEntity) = withContext(IO) {
        (YouTube.browse(BrowseEndpoint(browseId = album.id)).items.firstOrNull() as? AlbumOrPlaylistHeader)?.let { header ->
            albumDao.update(album.copy(
                title = header.name,
                thumbnailUrl = header.thumbnails.lastOrNull()?.url,
                year = header.year
            ))
        }
    }

    suspend fun deleteAlbums(albums: List<Album>) = withContext(IO) {
        albums.forEach { album ->
            val songs = songDao.getAlbumSongEntities(album.id)
            albumDao.delete(album.album)
            songDao.delete(songs)
        }
    }

    override fun getAllPlaylists(sortInfo: ISortInfo<PlaylistSortType>) = ListWrapper<Int, Playlist>(
        getList = { withContext(IO) { playlistDao.getAllPlaylistsAsList() } },
        getFlow = {
            if (sortInfo.type == PlaylistSortType.SONG_COUNT) {
                playlistDao.getAllPlaylistsAsFlow(SortInfo(PlaylistSortType.CREATE_DATE, true)).map { list ->
                    list.sortedBy { it.songCount }.reversed(sortInfo.isDescending)
                }
            } else {
                playlistDao.getAllPlaylistsAsFlow(sortInfo)
            }
        }
    )

    suspend fun getPlaylistCount() = withContext(IO) { playlistDao.getPlaylistCount() }

    suspend fun refetchPlaylist(playlist: Playlist) = withContext(IO) {
        (YouTube.browse(BrowseEndpoint(browseId = "VL" + playlist.id)).items.firstOrNull() as? AlbumOrPlaylistHeader)?.let { header ->
            playlistDao.upsert(header.toPlaylistEntity())
        }
    }

    override fun searchPlaylists(query: String) = ListWrapper<Int, PlaylistEntity>(
        getList = { withContext(IO) { playlistDao.searchPlaylists(query) } }
    )

    override suspend fun addPlaylist(playlist: PlaylistEntity): Unit = withContext(IO) { playlistDao.insert(playlist) }
    override suspend fun updatePlaylist(playlist: PlaylistEntity) = withContext(IO) { playlistDao.update(playlist) }
    override suspend fun deletePlaylists(playlists: List<PlaylistEntity>) = withContext(IO) { playlistDao.delete(playlists) }

    override suspend fun getPlaylistSongEntities(playlistId: String) = ListWrapper<Int, PlaylistSongMap>(
        getList = { withContext(IO) { playlistDao.getPlaylistSongEntities(playlistId) } }
    )

    override suspend fun updatePlaylistSongEntities(playlistSongEntities: List<PlaylistSongMap>) = withContext(IO) {
        playlistDao.updatePlaylistSongEntities(playlistSongEntities)
    }

    override suspend fun addSongsToPlaylist(playlistId: String, songIds: List<String>) {
        var maxId = playlistDao.getPlaylistMaxId(playlistId) ?: -1
        playlistDao.insertPlaylistSongEntities(songIds.map { songId ->
            PlaylistSongMap(
                playlistId = playlistId,
                songId = songId,
                idInPlaylist = ++maxId
            )
        })
    }

    suspend fun addToPlaylist(playlist: PlaylistEntity, items: List<LocalItem>) = withContext(IO) {
        val songIds = items.flatMap { item ->
            when (item) {
                is Song -> listOf(item).map { it.id }
                is Album -> getAlbumSongs(item.id).map { it.id }
                is Artist -> getArtistSongs(item.id, SongSortInfoPreference).getList().map { it.id }
                is Playlist -> if (item.playlist.isLocalPlaylist) {
                    getPlaylistSongs(item.id, SongSortInfoPreference).getList().map { it.id }
                } else {
                    safeAddSongs(YouTube.browseAll(BrowseEndpoint(browseId = "VL" + item.id)).filterIsInstance<SongItem>()).map { it.id }
                }
            }
        }
        addSongsToPlaylist(playlist.id, songIds)
    }

    suspend fun addToPlaylist(playlist: PlaylistEntity, item: YTItem) = withContext(IO) {
        if (playlist.isYouTubePlaylist) return@withContext
        val songs = when (item) {
            is ArtistItem -> return@withContext
            is SongItem -> YouTube.getQueue(videoIds = listOf(item.id))
            is AlbumItem -> YouTube.getQueue(playlistId = item.playlistId)
            is PlaylistItem -> YouTube.browseAll(BrowseEndpoint(browseId = "VL" + item.id)).filterIsInstance<SongItem>()
        }
        addSongs(songs)
        addSongsToPlaylist(playlist.id, songs.map { it.id })
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