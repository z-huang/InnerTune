package com.zionhuang.music.repos

import android.app.DownloadManager
import android.content.Context
import android.net.ConnectivityManager
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
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.db.entities.ArtistEntity.Companion.generateArtistId
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.generatePlaylistId
import com.zionhuang.music.extensions.*
import com.zionhuang.music.models.DataWrapper
import com.zionhuang.music.models.ListWrapper
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.models.sortInfo.*
import com.zionhuang.music.playback.SongPlayer
import com.zionhuang.music.repos.base.LocalRepository
import com.zionhuang.music.ui.bindings.resizeThumbnailUrl
import com.zionhuang.music.utils.md5
import com.zionhuang.music.utils.preference.enumPreference
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime

class SongRepository(private val context: Context) : LocalRepository {
    private val database = MusicDatabase.getInstance(context)
    private val songDao = database.songDao
    private val artistDao = database.artistDao
    private val albumDao = database.albumDao
    private val playlistDao = database.playlistDao
    private val downloadDao = database.downloadDao
    private val searchHistoryDao = database.searchHistoryDao
    private val formatDao = database.formatDao
    private val lyricsDao = database.lyricsDao

    private val connectivityManager = context.getSystemService<ConnectivityManager>()!!
    private var autoDownload by context.preference(R.string.pref_auto_download, false)
    private var audioQuality by enumPreference(context, R.string.pref_audio_quality, SongPlayer.AudioQuality.AUTO)

    /**
     * Browse
     */
    override fun getAllSongs(sortInfo: ISortInfo<SongSortType>): ListWrapper<Int, Song> = ListWrapper(
        getFlow = {
            if (sortInfo.type == SongSortType.ARTIST) {
                songDao.getAllSongsAsFlow(SortInfo(SongSortType.CREATE_DATE, true)).map { list ->
                    list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { song ->
                        song.artists.joinToString(separator = "") { it.name }
                    }).reversed(sortInfo.isDescending)
                }
            } else {
                songDao.getAllSongsAsFlow(sortInfo)
            }
        }
    )

    override suspend fun getSongCount() = withContext(IO) { songDao.getSongCount() }

    override fun getAllArtists(sortInfo: ISortInfo<ArtistSortType>) = ListWrapper<Int, Artist>(
        getFlow = {
            if (sortInfo.type == ArtistSortType.SONG_COUNT) {
                artistDao.getAllArtistsAsFlow(SortInfo(ArtistSortType.CREATE_DATE, true)).map { list ->
                    list.sortedBy { it.songCount }.reversed(sortInfo.isDescending)
                }
            } else {
                artistDao.getAllArtistsAsFlow(sortInfo)
            }
        }
    )

    override suspend fun getArtistCount() = withContext(IO) { artistDao.getArtistCount() }

    override suspend fun getArtistSongsPreview(artistId: String): Result<List<YTBaseItem>> = withContext(IO) {
        runCatching {
            if (artistDao.hasArtist(artistId)) {
                listOf(Header(
                    title = context.getString(R.string.header_from_your_library),
                    moreNavigationEndpoint = NavigationEndpoint(
                        browseLocalArtistSongsEndpoint = BrowseLocalArtistSongsEndpoint(artistId)
                    )
                )) + YouTube.getQueue(videoIds = songDao.getArtistSongsPreview(artistId)).getOrThrow()
            } else {
                emptyList()
            }
        }
    }

    override fun getArtistSongs(artistId: String, sortInfo: ISortInfo<SongSortType>): ListWrapper<Int, Song> = ListWrapper(
        getList = { withContext(IO) { songDao.getArtistSongsAsList(artistId, sortInfo) } },
        getFlow = {
            songDao.getArtistSongsAsFlow(artistId, if (sortInfo.type == SongSortType.ARTIST) SortInfo(SongSortType.CREATE_DATE, sortInfo.isDescending) else sortInfo)
        }
    )

    override suspend fun getArtistSongCount(artistId: String) = withContext(IO) { songDao.getArtistSongCount(artistId) }

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

    override suspend fun getAlbumCount() = withContext(IO) { albumDao.getAlbumCount() }
    override suspend fun getAlbumSongs(albumId: String) = withContext(IO) {
        songDao.getAlbumSongs(albumId)
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

    override fun getPlaylistSongs(playlistId: String): ListWrapper<Int, Song> = ListWrapper(
        getList = { withContext(IO) { songDao.getPlaylistSongsAsList(playlistId) } },
        getFlow = { songDao.getPlaylistSongsAsFlow(playlistId) }
    )

    override fun getLikedSongs(sortInfo: ISortInfo<SongSortType>): ListWrapper<Int, Song> = ListWrapper(
        getFlow = {
            if (sortInfo.type == SongSortType.ARTIST) {
                songDao.getLikedSongs(SortInfo(SongSortType.CREATE_DATE, true)).map { list ->
                    list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { song ->
                        song.artists.joinToString(separator = "") { it.name }
                    }).reversed(sortInfo.isDescending)
                }
            } else {
                songDao.getLikedSongs(sortInfo)
            }
        }
    )

    override fun getLikedSongCount(): Flow<Int> = songDao.getLikedSongCount()

    override fun getDownloadedSongs(sortInfo: ISortInfo<SongSortType>): ListWrapper<Int, Song> = ListWrapper(
        getList = {
            withContext(IO) {
                if (sortInfo.type == SongSortType.ARTIST) {
                    songDao.getDownloadedSongsAsList(SortInfo(SongSortType.CREATE_DATE, true))
                        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { song ->
                            song.artists.joinToString(separator = "") { it.name }
                        }).reversed(sortInfo.isDescending)
                } else {
                    songDao.getDownloadedSongsAsList(sortInfo)
                }
            }
        },
        getFlow = {
            if (sortInfo.type == SongSortType.ARTIST) {
                songDao.getDownloadedSongsAsFlow(SortInfo(SongSortType.CREATE_DATE, true)).map { list ->
                    list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { song ->
                        song.artists.joinToString(separator = "") { it.name }
                    }).reversed(sortInfo.isDescending)
                }
            } else {
                songDao.getDownloadedSongsAsFlow(sortInfo)
            }
        }
    )

    override fun getDownloadedSongCount(): Flow<Int> = songDao.getDownloadedSongCount()

    /**
     * Search
     */
    override fun searchAll(query: String): Flow<List<LocalBaseItem>> = combine(
        songDao.searchSongsPreview(query, 3).map { if (it.isNotEmpty()) listOf(TextHeader(context.getString(R.string.search_filter_songs))) + it else emptyList() },
        artistDao.searchArtistsPreview(query, 3).map { if (it.isNotEmpty()) listOf(TextHeader(context.getString(R.string.search_filter_artists))) + it else emptyList() },
        albumDao.searchAlbumsPreview(query, 3).map { if (it.isNotEmpty()) listOf(TextHeader(context.getString(R.string.search_filter_albums))) + it else emptyList() },
        playlistDao.searchPlaylistsPreview(query, 3).map { if (it.isNotEmpty()) listOf(TextHeader(context.getString(R.string.search_filter_playlists))) + it else emptyList() }
    ) { songResult, artistResult, albumResult, playlistResult -> songResult + artistResult + albumResult + playlistResult }

    override fun searchSongs(query: String) = songDao.searchSongs(query)
    override fun searchDownloadedSongs(query: String) = songDao.searchDownloadedSongs(query)
    override fun searchArtists(query: String) = artistDao.searchArtists(query)
    override fun searchAlbums(query: String) = albumDao.searchAlbums(query)
    override fun searchPlaylists(query: String) = playlistDao.searchPlaylists(query)

    /**
     * Song
     */
    override suspend fun addSong(mediaMetadata: MediaMetadata): SongEntity = withContext(IO) {
        songDao.getSong(mediaMetadata.id)?.let {
            return@withContext it.song
        }
        val song = mediaMetadata.toSongEntity()
        songDao.insert(song)
        mediaMetadata.artists.forEachIndexed { index, artist ->
            artistDao.insert(ArtistEntity(
                id = artist.id,
                name = artist.name
            ))
            artistDao.insert(SongArtistMap(
                songId = mediaMetadata.id,
                artistId = artist.id,
                position = index
            ))
        }
        if (autoDownload) downloadSong(song)
        song
    }

    private suspend fun addSongs(items: List<SongItem>) = withContext(IO) {
        val songs = items.map { it.toSongEntity() }
        val songArtistMaps = items.flatMap { song ->
            song.artists.mapIndexed { index, run ->
                val artistId = (run.navigationEndpoint?.browseEndpoint?.browseId ?: getArtistByName(run.text)?.id ?: generateArtistId()).also {
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

    override suspend fun safeAddSongs(songs: List<SongItem>): List<SongEntity> = withContext(IO) {
        // call [YouTube.getQueue] to ensure we get full information
        addSongs(songs.chunked(MAX_GET_QUEUE_SIZE).flatMap { chunk ->
            YouTube.getQueue(chunk.map { it.id }).getOrThrow()
        })
    }

    override suspend fun refetchSongs(songs: List<Song>) = withContext(IO) {
        val map = songs.associateBy { it.id }
        val songItems = songs.chunked(MAX_GET_QUEUE_SIZE).flatMap { chunk ->
            YouTube.getQueue(chunk.map { it.id }).getOrThrow()
        }
        songDao.update(songItems.map { item ->
            map[item.id]!!.song.copy(
                id = item.id,
                title = item.title,
                duration = item.duration!!,
                thumbnailUrl = item.thumbnails.last().url,
                albumId = item.album?.navigationEndpoint?.browseId,
                albumName = item.album?.text,
                modifyDate = LocalDateTime.now()
            )
        })
        val songArtistMaps = songItems.flatMap { song ->
            song.artists.mapIndexed { index, run ->
                val artistId = (run.navigationEndpoint?.browseEndpoint?.browseId ?: getArtistByName(run.text)?.id ?: generateArtistId()).also {
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
        artistDao.deleteSongArtists(songs.map { it.id })
        artistDao.insert(songArtistMaps)
        artistDao.delete(songs
            .flatMap { it.artists }
            .distinctBy { it.id }
            .filter { artistDao.getArtistSongCount(it.id) == 0 }
        )
    }

    override fun getSongById(songId: String?) = DataWrapper(
        getValueAsync = { withContext(IO) { songDao.getSong(songId) } },
        getLiveData = { songDao.getSongAsLiveData(songId).distinctUntilChanged() },
        getFlow = { songDao.getSongAsFlow(songId) }
    )

    override fun getSongFile(songId: String): File {
        val mediaDir = context.getExternalFilesDir(null)!! / "media"
        if (!mediaDir.isDirectory) mediaDir.mkdirs()
        return mediaDir / md5(songId)
    }

    private fun getSongTempFile(songId: String): File {
        val mediaDir = context.getExternalFilesDir(null)!! / "media"
        if (!mediaDir.isDirectory) mediaDir.mkdirs()
        return mediaDir / (md5(songId) + ".tmp")
    }

    override fun hasSong(songId: String): DataWrapper<Boolean> = DataWrapper(
        getValueAsync = { songDao.hasSong(songId) },
        getLiveData = { songDao.hasSongAsLiveData(songId).distinctUntilChanged() }
    )

    override suspend fun incrementSongTotalPlayTime(songId: String, playTime: Long) = withContext(IO) {
        songDao.incrementSongTotalPlayTime(songId, playTime)
    }

    override suspend fun updateSongTitle(song: Song, newTitle: String) = withContext(IO) {
        songDao.update(song.song.copy(
            title = newTitle,
            modifyDate = LocalDateTime.now()
        ))
    }

    override suspend fun toggleLiked(songs: List<Song>) = withContext(IO) {
        songDao.update(songs.map {
            it.song.copy(
                liked = !it.song.liked,
                modifyDate = LocalDateTime.now()
            )
        })
    }

    override suspend fun downloadSongs(songs: List<SongEntity>) = withContext(IO) {
        songs.filter { it.downloadState == STATE_NOT_DOWNLOADED }.let { songs ->
            songDao.update(songs.map { it.copy(downloadState = STATE_PREPARING) })
            songs.forEach { song ->
                val playedFormat = getSongFormat(song.id).getValueAsync()
                val playerResponse = YouTube.player(videoId = song.id).getOrThrow()
                if (playerResponse.playabilityStatus.status == "OK") {
                    val format = if (playedFormat != null) {
                        playerResponse.streamingData?.adaptiveFormats?.find { it.itag == playedFormat.itag }
                    } else {
                        playerResponse.streamingData?.adaptiveFormats
                            ?.filter { it.isAudio }
                            ?.maxByOrNull {
                                it.bitrate * when (audioQuality) {
                                    SongPlayer.AudioQuality.AUTO -> if (connectivityManager.isActiveNetworkMetered) -1 else 1
                                    SongPlayer.AudioQuality.HIGH -> 1
                                    SongPlayer.AudioQuality.LOW -> -1
                                }
                            }
                    }
                    if (format == null) {
                        songDao.update(song.copy(downloadState = STATE_NOT_DOWNLOADED))
                        // TODO
                    } else {
                        upsert(FormatEntity(
                            id = song.id,
                            itag = format.itag,
                            mimeType = format.mimeType.split(";")[0],
                            codecs = format.mimeType.split("codecs=")[1].removeSurrounding("\""),
                            bitrate = format.bitrate,
                            sampleRate = format.audioSampleRate,
                            contentLength = format.contentLength!!,
                            loudnessDb = playerResponse.playerConfig?.audioConfig?.loudnessDb
                        ))
                        songDao.update(song.copy(downloadState = STATE_DOWNLOADING))
                        val downloadManager = context.getSystemService<DownloadManager>()!!
                        val req = DownloadManager.Request(format.url.toUri())
                            .setTitle(song.title)
                            .setDestinationUri(getSongTempFile(song.id).toUri())
                        val did = downloadManager.enqueue(req)
                        addDownloadEntity(DownloadEntity(did, song.id))
                    }
                } else {
                    songDao.update(song.copy(downloadState = STATE_NOT_DOWNLOADED))
                    // TODO
                }
            }
        }
    }

    override suspend fun onDownloadComplete(downloadId: Long, success: Boolean): Unit = withContext(IO) {
        getDownloadEntity(downloadId)?.songId?.let { songId ->
            songDao.getSong(songId)?.let { song ->
                songDao.update(song.song.copy(downloadState = if (success) STATE_DOWNLOADED else STATE_NOT_DOWNLOADED))
                getSongTempFile(songId).renameTo(getSongFile(songId))
            }
            removeDownloadEntity(downloadId)
        }
    }

    override suspend fun validateDownloads() {
        getDownloadedSongs(SongSortInfoPreference).getList().forEach { song ->
            if (!getSongFile(song.id).exists() && !getSongTempFile(song.id).exists()) {
                songDao.update(song.song.copy(downloadState = STATE_NOT_DOWNLOADED))
            }
        }
    }

    override suspend fun removeDownloads(songs: List<Song>) = withContext(IO) {
        songs.forEach { song ->
            if (getSongFile(song.song.id).exists()) {
                getSongFile(song.song.id).delete()
            }
            songDao.update(song.song.copy(downloadState = STATE_NOT_DOWNLOADED))
        }
    }

    override suspend fun moveToTrash(songs: List<Song>) = withContext(IO) {
        songDao.update(songs.map { it.song.copy(isTrash = true) })
    }

    override suspend fun restoreFromTrash(songs: List<Song>) = withContext(IO) {
        songDao.update(songs.map { it.song.copy(isTrash = false) })
    }

    override suspend fun deleteSongs(songs: List<Song>) = withContext(IO) {
        val deletableSongs = songs.filter { it.album == null }
        val renewPlaylists = playlistDao.getPlaylistSongMaps(deletableSongs.map { it.id }).groupBy { it.playlistId }.mapValues { entry ->
            entry.value.minOf { it.position } - 1
        }
        songDao.delete(deletableSongs.map { it.song })
        deletableSongs.forEach { song ->
            getSongFile(song.song.id).delete()
        }
        artistDao.delete(deletableSongs
            .flatMap { it.artists }
            .distinctBy { it.id }
            .filter { artistDao.getArtistSongCount(it.id) == 0 })
        renewPlaylists.forEach { (playlistId, position) ->
            playlistDao.renewSongPositions(playlistId, position)
        }
    }

    /**
     * Artist
     */
    override suspend fun getArtistById(artistId: String): ArtistEntity? = withContext(IO) {
        artistDao.getArtistById(artistId)
    }

    override suspend fun getArtistByName(name: String): ArtistEntity? = withContext(IO) {
        artistDao.getArtistByName(name)
    }

    override suspend fun refetchArtists(artists: List<ArtistEntity>) = withContext(IO) {
        artists.forEach { artist ->
            if (artist.isYouTubeArtist) {
                val browseResult = YouTube.browse(BrowseEndpoint(browseId = artist.id)).getOrThrow()
                val header = browseResult.items.firstOrNull()
                if (header is ArtistHeader) {
                    artistDao.update(artist.copy(
                        name = header.name,
                        thumbnailUrl = header.bannerThumbnails?.lastOrNull()?.url?.let { resizeThumbnailUrl(it, 400, 400) },
                        bannerUrl = header.bannerThumbnails?.lastOrNull()?.url,
                        description = header.description,
                        lastUpdateTime = LocalDateTime.now()
                    ))
                }
            }
        }
    }

    override suspend fun updateArtist(artist: ArtistEntity) = withContext(IO) {
        artistDao.update(artist)
    }

    /**
     * Album
     */
    override suspend fun addAlbums(albums: List<AlbumItem>) = withContext(IO) {
        albums.forEach { album ->
            val ids = YouTube.browse(BrowseEndpoint(browseId = "VL" + album.playlistId)).getOrThrow().items.filterIsInstance<SongItem>().map { it.id }
            YouTube.getQueue(videoIds = ids).getOrThrow().let { songs ->
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
            (YouTube.browse(BrowseEndpoint(browseId = album.id)).getOrThrow().items.firstOrNull() as? AlbumOrPlaylistHeader)?.artists?.forEachIndexed { index, run ->
                val artistId = (run.navigationEndpoint?.browseEndpoint?.browseId ?: getArtistByName(run.text)?.id ?: generateArtistId()).also {
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

    override suspend fun refetchAlbums(albums: List<AlbumEntity>) = withContext(IO) {
        albums.forEach { album ->
            (YouTube.browse(BrowseEndpoint(browseId = album.id)).getOrThrow().items.firstOrNull() as? AlbumOrPlaylistHeader)?.let { header ->
                albumDao.update(album.copy(
                    title = header.name,
                    thumbnailUrl = header.thumbnails.lastOrNull()?.url,
                    year = header.year,
                    lastUpdateTime = LocalDateTime.now()
                ))
            }
        }
    }

    override suspend fun getAlbum(albumId: String) = withContext(IO) {
        albumDao.getAlbumById(albumId)
    }

    override suspend fun deleteAlbums(albums: List<Album>) = withContext(IO) {
        albums.forEach { album ->
            val songs = songDao.getAlbumSongs(album.id).map { it.copy(album = null) }
            albumDao.delete(album.album)
            deleteSongs(songs)
            artistDao.delete(album.artists.filter { artistDao.getArtistSongCount(it.id) == 0 })
        }
    }

    /**
     * Playlist
     */
    override suspend fun insertPlaylist(playlist: PlaylistEntity): Unit = withContext(IO) { playlistDao.insert(playlist) }
    override suspend fun addPlaylists(playlists: List<PlaylistItem>) = withContext(IO) {
        playlists.forEach { playlist ->
            (YouTube.browse(BrowseEndpoint(browseId = "VL" + playlist.id)).getOrThrow().items.firstOrNull() as? AlbumOrPlaylistHeader)?.let { header ->
                playlistDao.insert(header.toPlaylistEntity())
            }
        }
    }

    override suspend fun importPlaylists(playlists: List<PlaylistItem>) = withContext(IO) {
        playlists.forEach { playlist ->
            val playlistId = generatePlaylistId()
            playlistDao.insert(playlist.toPlaylistEntity().copy(id = playlistId))
            var index = 0
            val songs = YouTube.browseAll(BrowseEndpoint(browseId = "VL" + playlist.id)).getOrThrow().filterIsInstance<SongItem>()
            safeAddSongs(songs)
            playlistDao.insert(songs.map {
                PlaylistSongMap(
                    playlistId = playlistId,
                    songId = it.id,
                    position = index++
                )
            })
        }
    }

    private suspend fun addSongsToPlaylist(playlistId: String, songIds: List<String>) {
        var maxId = playlistDao.getPlaylistMaxId(playlistId) ?: -1
        playlistDao.insert(songIds.map { songId ->
            PlaylistSongMap(
                playlistId = playlistId,
                songId = songId,
                position = ++maxId
            )
        })
    }

    override suspend fun addToPlaylist(playlist: PlaylistEntity, items: List<LocalItem>) = withContext(IO) {
        val songIds = items.flatMap { item ->
            when (item) {
                is Song -> listOf(item).map { it.id }
                is Album -> getAlbumSongs(item.id).map { it.id }
                is Artist -> getArtistSongs(item.id, SongSortInfoPreference).getList().map { it.id }
                is Playlist -> if (item.playlist.isLocalPlaylist) {
                    getPlaylistSongs(item.id).getList().map { it.id }
                } else {
                    safeAddSongs(YouTube.browseAll(BrowseEndpoint(browseId = "VL" + item.id)).getOrThrow().filterIsInstance<SongItem>()).map { it.id }
                }
            }
        }
        addSongsToPlaylist(playlist.id, songIds)
    }

    override suspend fun addYouTubeItemsToPlaylist(playlist: PlaylistEntity, items: List<YTItem>) = withContext(IO) {
        val songs = items.flatMap { item ->
            when (item) {
                is SongItem -> YouTube.getQueue(videoIds = listOf(item.id)).getOrThrow()
                is AlbumItem -> YouTube.browse(BrowseEndpoint(browseId = "VL" + item.playlistId)).getOrThrow().items.filterIsInstance<SongItem>() // consider refetch by [YouTube.getQueue] if needed
                is PlaylistItem -> YouTube.getQueue(playlistId = item.id).getOrThrow()
                is ArtistItem -> emptyList()
            }
        }
        addSongs(songs)
        addSongsToPlaylist(playlist.id, songs.map { it.id })
    }

    override suspend fun addMediaItemToPlaylist(playlist: PlaylistEntity, item: MediaMetadata) = withContext(IO) {
        val song = YouTube.getQueue(videoIds = listOf(item.id)).getOrThrow()
        addSongs(song)
        addSongsToPlaylist(playlist.id, song.map { it.id })
    }

    override suspend fun refetchPlaylists(playlists: List<Playlist>): Unit = withContext(IO) {
        playlists.forEach { playlist ->
            (YouTube.browse(BrowseEndpoint(browseId = "VL" + playlist.id)).getOrThrow().items.firstOrNull() as? AlbumOrPlaylistHeader)?.let { header ->
                playlistDao.update(playlist.playlist.copy(
                    name = header.name,
                    author = header.artists?.firstOrNull()?.text,
                    authorId = header.artists?.firstOrNull()?.navigationEndpoint?.browseEndpoint?.browseId,
                    year = header.year,
                    thumbnailUrl = header.thumbnails.lastOrNull()?.url,
                    lastUpdateTime = LocalDateTime.now()
                ))
            }
        }
    }

    override suspend fun downloadPlaylists(playlists: List<Playlist>) = withContext(IO) {
        downloadSongs(playlists
            .filter { it.playlist.isLocalPlaylist }
            .flatMap { getPlaylistSongs(it.id).getList() }
            .distinctBy { it.id }
            .map { it.song })
    }

    override suspend fun getPlaylistById(playlistId: String): Playlist = withContext(IO) {
        playlistDao.getPlaylistById(playlistId)
    }

    override suspend fun updatePlaylist(playlist: PlaylistEntity) = withContext(IO) { playlistDao.update(playlist) }

    override suspend fun movePlaylistItems(playlistId: String, from: Int, to: Int) = withContext(IO) {
        val target = playlistDao.getPlaylistSongMap(playlistId, from) ?: return@withContext
        if (to < from) {
            playlistDao.incrementSongPositions(playlistId, to, from - 1)
        } else if (from < to) {
            playlistDao.decrementSongPositions(playlistId, from + 1, to)
        }
        playlistDao.update(target.copy(position = to))
    }

    override suspend fun removeSongFromPlaylist(playlistId: String, position: Int) = withContext(IO) {
        playlistDao.deletePlaylistSong(playlistId, position)
        playlistDao.decrementSongPositions(playlistId, position + 1)
    }

    override suspend fun removeSongsFromPlaylist(playlistId: String, positions: List<Int>) = withContext(IO) {
        playlistDao.deletePlaylistSong(playlistId, positions)
        playlistDao.renewSongPositions(playlistId, positions.minOrNull()!! - 1)
    }

    override suspend fun deletePlaylists(playlists: List<PlaylistEntity>) = withContext(IO) {
        playlistDao.delete(playlists)
    }

    /**
     * Download
     */
    override suspend fun addDownloadEntity(item: DownloadEntity) = withContext(IO) { downloadDao.insert(item) }
    override suspend fun getDownloadEntity(downloadId: Long): DownloadEntity? = withContext(IO) { downloadDao.getDownloadEntity(downloadId) }
    override suspend fun removeDownloadEntity(downloadId: Long) = withContext(IO) { downloadDao.delete(downloadId) }

    /**
     * Search history
     */
    override suspend fun getAllSearchHistory() = withContext(IO) {
        searchHistoryDao.getAllHistory()
    }

    override suspend fun getSearchHistory(query: String) = withContext(IO) {
        searchHistoryDao.getHistory(query)
    }

    override suspend fun insertSearchHistory(query: String) = withContext(IO) {
        searchHistoryDao.insert(SearchHistory(query = query))
    }

    override suspend fun deleteSearchHistory(query: String) = withContext(IO) {
        searchHistoryDao.delete(query)
    }

    override suspend fun clearSearchHistory() {
        searchHistoryDao.clearHistory()
    }

    /**
     * Format
     */
    override fun getSongFormat(songId: String?): DataWrapper<FormatEntity?> = DataWrapper(
        getValueAsync = { withContext(IO) { formatDao.getSongFormat(songId) } },
        getFlow = { formatDao.getSongFormatAsFlow(songId) }
    )

    override suspend fun upsert(format: FormatEntity) = withContext(IO) {
        formatDao.upsert(format)
    }

    /**
     * Lyrics
     */
    override fun getLyrics(songId: String?): Flow<LyricsEntity?> =
        lyricsDao.getLyricsAsFlow(songId)

    override suspend fun hasLyrics(songId: String): Boolean = withContext(IO) {
        lyricsDao.hasLyrics(songId)
    }

    override suspend fun upsert(lyrics: LyricsEntity) = withContext(IO) {
        lyricsDao.upsert(lyrics)
    }
}