package com.zionhuang.music.repos

import android.content.Context
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.YouTube.MAX_GET_QUEUE_SIZE
import com.zionhuang.innertube.models.*
import com.zionhuang.music.constants.AUTO_ADD_TO_LIBRARY
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.db.entities.Album
import com.zionhuang.music.db.entities.Artist
import com.zionhuang.music.db.entities.ArtistEntity.Companion.generateArtistId
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.generatePlaylistId
import com.zionhuang.music.extensions.div
import com.zionhuang.music.extensions.preference
import com.zionhuang.music.extensions.reversed
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.models.sortInfo.*
import com.zionhuang.music.models.toMediaMetadata
import com.zionhuang.music.repos.base.LocalRepository
import com.zionhuang.music.ui.utils.resize
import com.zionhuang.music.utils.md5
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
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

    private var autoDownload by context.preference(AUTO_ADD_TO_LIBRARY, false)

    override fun getAllSongId(): Flow<List<String>> = songDao.getAllSongId()
    override fun getAllLikedSongId(): Flow<List<String>> = songDao.getAllLikedSongId()
    override fun getAllAlbumId(): Flow<List<String>> = albumDao.getAllAlbumId()
    override fun getAllPlaylistId(): Flow<List<String>> = playlistDao.getAllPlaylistId()

    /**
     * Browse
     */
    override fun getAllSongs(sortInfo: ISortInfo<SongSortType>) =
        if (sortInfo.type == SongSortType.ARTIST) {
            songDao.getAllSongsAsFlow(SortInfo(SongSortType.CREATE_DATE, true)).map { list ->
                list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { song ->
                    song.artists.joinToString(separator = "") { it.name }
                }).reversed(sortInfo.isDescending)
            }
        } else {
            songDao.getAllSongsAsFlow(sortInfo)
        }

    override suspend fun getSongCount() = withContext(IO) { songDao.getSongCount() }

    override fun getAllArtists(sortInfo: ISortInfo<ArtistSortType>) =
        if (sortInfo.type == ArtistSortType.SONG_COUNT) {
            artistDao.getAllArtistsAsFlow(SortInfo(ArtistSortType.CREATE_DATE, true)).map { list ->
                list.sortedBy { it.songCount }.reversed(sortInfo.isDescending)
            }
        } else {
            artistDao.getAllArtistsAsFlow(sortInfo)
        }

    override suspend fun getArtistCount() = withContext(IO) { artistDao.getArtistCount() }

    override suspend fun getArtistSongsPreview(artistId: String) = songDao.getArtistSongsPreview(artistId)

    override fun getArtistSongs(artistId: String, sortInfo: ISortInfo<SongSortType>) =
        songDao.getArtistSongsAsFlow(artistId, if (sortInfo.type == SongSortType.ARTIST) SortInfo(SongSortType.CREATE_DATE, sortInfo.isDescending) else sortInfo)

    override suspend fun getArtistSongCount(artistId: String) = withContext(IO) { songDao.getArtistSongCount(artistId) }

    override fun getAllAlbums(sortInfo: ISortInfo<AlbumSortType>) =
        if (sortInfo.type == AlbumSortType.ARTIST) {
            albumDao.getAllAlbumsAsFlow(SortInfo(AlbumSortType.CREATE_DATE, true)).map { list ->
                list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { song ->
                    song.artists.joinToString(separator = "") { it.name }
                }).reversed(sortInfo.isDescending)
            }
        } else {
            albumDao.getAllAlbumsAsFlow(sortInfo)
        }

    override suspend fun getAlbumCount() = withContext(IO) { albumDao.getAlbumCount() }
    override suspend fun getAlbumSongs(albumId: String) = withContext(IO) {
        songDao.getAlbumSongs(albumId)
    }

    override fun getAllPlaylists(sortInfo: ISortInfo<PlaylistSortType>) =
        if (sortInfo.type == PlaylistSortType.SONG_COUNT) {
            playlistDao.getAllPlaylistsAsFlow(SortInfo(PlaylistSortType.CREATE_DATE, true)).map { list ->
                list.sortedBy { it.songCount }.reversed(sortInfo.isDescending)
            }
        } else {
            playlistDao.getAllPlaylistsAsFlow(sortInfo)
        }

    override fun getPlaylistSongs(playlistId: String) = songDao.getPlaylistSongsAsFlow(playlistId)

    override fun getLikedSongs(sortInfo: ISortInfo<SongSortType>) =
        if (sortInfo.type == SongSortType.ARTIST) {
            songDao.getLikedSongs(SortInfo(SongSortType.CREATE_DATE, true)).map { list ->
                list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { song ->
                    song.artists.joinToString(separator = "") { it.name }
                }).reversed(sortInfo.isDescending)
            }
        } else {
            songDao.getLikedSongs(sortInfo)
        }

    override fun getLikedSongCount(): Flow<Int> = songDao.getLikedSongCount()

    override fun getDownloadedSongs(sortInfo: ISortInfo<SongSortType>) =
        if (sortInfo.type == SongSortType.ARTIST) {
            songDao.getDownloadedSongsAsFlow(SortInfo(SongSortType.CREATE_DATE, true)).map { list ->
                list.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { song ->
                    song.artists.joinToString(separator = "") { it.name }
                }).reversed(sortInfo.isDescending)
            }
        } else {
            songDao.getDownloadedSongsAsFlow(sortInfo)
        }

    override fun getDownloadedSongCount(): Flow<Int> = songDao.getDownloadedSongCount()

    /**
     * Search
     */
    override fun searchAll(query: String): Flow<List<LocalItem>> = combine(
        songDao.searchSongsPreview(query, PREVIEW_SIZE),
        artistDao.searchArtistsPreview(query, PREVIEW_SIZE),
        albumDao.searchAlbumsPreview(query, PREVIEW_SIZE),
        playlistDao.searchPlaylistsPreview(query, PREVIEW_SIZE)
    ) { songResult, artistResult, albumResult, playlistResult -> songResult + artistResult + albumResult + playlistResult }

    override fun searchSongs(query: String) = songDao.searchSongs(query)
    override fun searchDownloadedSongs(query: String) = songDao.searchDownloadedSongs(query)
    override fun searchArtists(query: String) = artistDao.searchArtists(query)
    override fun searchAlbums(query: String) = albumDao.searchAlbums(query)
    override fun searchPlaylists(query: String) = playlistDao.searchPlaylists(query)

    /**
     * Song
     */
    override suspend fun addSongs(songs: List<MediaMetadata>) = withContext(IO) {
        songs.forEach { mediaMetadata ->
            songDao.getSong(mediaMetadata.id)?.let { song ->
                if (song.song.downloadState == STATE_NOT_DOWNLOADED && autoDownload) {
                    downloadSong(mediaMetadata.id)
                }
                return@forEach
            }
            songDao.insert(mediaMetadata.toSongEntity())
            mediaMetadata.artists.forEachIndexed { index, artist ->
                val artistId = artist.id ?: getArtistByName(artist.name)?.id ?: generateArtistId()
                artistDao.insert(ArtistEntity(
                    id = artistId,
                    name = artist.name
                ))
                artistDao.insert(SongArtistMap(
                    songId = mediaMetadata.id,
                    artistId = artistId,
                    position = index
                ))
            }
            if (autoDownload) downloadSong(mediaMetadata.id)
        }
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
                thumbnailUrl = item.thumbnail,
                albumId = item.album?.id,
                albumName = item.album?.name,
                modifyDate = LocalDateTime.now()
            )
        })
        val songArtistMaps = songItems.flatMap { song ->
            song.artists.mapIndexed { index, artist ->
                val artistId = (artist.id ?: getArtistByName(artist.name)?.id ?: generateArtistId()).also {
                    artistDao.insert(ArtistEntity(
                        id = it,
                        name = artist.name
                    ))
                }
                SongArtistMap(
                    songId = song.id,
                    artistId = artistId,
                    position = index
                )
            }
        }
        songs.forEach { song ->
            artistDao.deleteSongArtistMaps(songId = song.id)
        }
        artistDao.insertSongArtistMaps(songArtistMaps)
        artistDao.delete(
            songs
                .flatMap { it.artists }
                .distinctBy { it.id }
                .filter { artistDao.getArtistSongCount(it.id) == 0 }
        )
    }

    override fun getSongById(songId: String?) = songDao.getSongAsFlow(songId)

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

    override suspend fun incrementSongTotalPlayTime(songId: String, playTime: Long) = withContext(IO) {
        songDao.incrementSongTotalPlayTime(songId, playTime)
    }

    override suspend fun updateSongDuration(songId: String, duration: Int) = withContext(IO) {
        songDao.updateSongDuration(songId, duration)
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

    override suspend fun downloadSongs(songIds: List<String>) = withContext(IO) {
        // TODO
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
        getDownloadedSongs(SongSortInfoPreference).first().forEach { song ->
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

    override suspend fun deleteSong(songId: String) {
        songDao.delete(songId)
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
        artists.filter { artist ->
            artist.isYouTubeArtist
        }.forEach { artist ->
            val artistPage = YouTube.browseArtist(artist.id).getOrThrow()
            artistDao.update(artist.copy(
                name = artistPage.artist.title,
                thumbnailUrl = artistPage.artist.thumbnail.resize(400, 400),
                lastUpdateTime = LocalDateTime.now()
            ))
        }
    }

    override suspend fun updateArtist(artist: ArtistEntity) = withContext(IO) {
        artistDao.update(artist)
    }

    /**
     * Album
     */
    override suspend fun addAlbums(albums: List<AlbumItem>) = withContext(IO) {
        albums.filter {
            albumDao.getAlbumById(it.id) == null
        }.forEach { album ->
            val albumPage = YouTube.browseAlbum(album.browseId).getOrThrow()
            albumDao.insert(AlbumEntity(
                id = albumPage.album.browseId,
                title = albumPage.album.title,
                year = albumPage.album.year,
                thumbnailUrl = albumPage.album.thumbnail,
                songCount = albumPage.songs.size,
                duration = albumPage.songs.sumOf { it.duration ?: 0 }
            ))
            addSongs(albumPage.songs.map(SongItem::toMediaMetadata))
            albumDao.upsert(albumPage.songs.mapIndexed { index, song ->
                SongAlbumMap(
                    songId = song.id,
                    albumId = album.id,
                    index = index
                )
            })
            artistDao.insertArtists(albumPage.album.artists!!.map { artist ->
                ArtistEntity(
                    id = artist.id ?: getArtistByName(artist.name)?.id ?: generateArtistId(),
                    name = artist.name
                )
            })
            albumDao.insertAlbumArtistMaps(albumPage.album.artists!!.mapIndexed { index, artist ->
                AlbumArtistMap(
                    albumId = album.id,
                    artistId = artist.id ?: getArtistByName(artist.name)?.id ?: generateArtistId(),
                    order = index
                )
            })
        }
    }

    override suspend fun getAlbum(albumId: String) = withContext(IO) {
        albumDao.getAlbumById(albumId)
    }

    override suspend fun getAlbumWithSongs(albumId: String) = withContext(IO) {
        albumDao.getAlbumWithSongs(albumId)
    }

    override suspend fun deleteAlbum(albumId: String) = withContext(IO) {
        albumDao.delete(albumId)
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
    override suspend fun addPlaylists(playlists: List<PlaylistItem>) = withContext(IO) {
        playlists.forEach { playlist ->
            val playlistPage = YouTube.browsePlaylist("VL${playlist.id}").getOrThrow()
            playlistDao.insert(PlaylistEntity(
                id = playlistPage.playlist.id,
                name = playlistPage.playlist.title,
                author = playlistPage.playlist.author.name,
                authorId = playlistPage.playlist.author.id,
                thumbnailUrl = playlistPage.playlist.thumbnail
            ))
        }
    }

    override suspend fun importPlaylists(playlists: List<PlaylistItem>) = withContext(IO) {
        playlists.forEach { playlist ->
            val playlistId = generatePlaylistId()
            val playlistPage = YouTube.browsePlaylist("VL${playlist.id}").getOrThrow()
            playlistDao.insert(PlaylistEntity(
                id = playlistId,
                name = playlistPage.playlist.title,
                thumbnailUrl = playlistPage.playlist.thumbnail
            ))
            var index = 0
            var songs: List<SongItem> = playlistPage.songs
            var continuation = playlistPage.songsContinuation
            while (true) {
                playlistDao.insert(songs.map {
                    PlaylistSongMap(
                        playlistId = playlistId,
                        songId = it.id,
                        position = index++
                    )
                })
                if (continuation == null) break
                val continuationPage = YouTube.browsePlaylistContinuation(continuation).getOrThrow()
                songs = continuationPage.songs
                continuation = continuationPage.continuation
            }
        }
    }

    override suspend fun insertPlaylist(playlist: PlaylistEntity): Unit = withContext(IO) { playlistDao.insert(playlist) }

    override fun getPlaylist(playlistId: String): Flow<Playlist> = playlistDao.getPlaylist(playlistId)

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
                is Song -> listOf(item.id)
                is Album -> getAlbumSongs(item.id).map { it.id }
                is Artist -> getArtistSongs(item.id, SongSortInfoPreference).first().map { it.id }
                is Playlist -> if (item.playlist.isLocalPlaylist) {
                    getPlaylistSongs(item.id).first().map { it.id }
                } else {
                    emptyList()
                }
            }
        }
        addSongsToPlaylist(playlist.id, songIds)
    }

    override suspend fun addYTItemsToPlaylist(playlist: PlaylistEntity, items: List<YTItem>) = withContext(IO) {
        val songs = items.flatMap { item ->
            when (item) {
                is SongItem -> listOf(item)
                is AlbumItem -> YouTube.browseAlbum("VL${item.playlistId}").getOrThrow().songs
                is PlaylistItem -> YouTube.getQueue(playlistId = item.id).getOrThrow()
                is ArtistItem -> emptyList()
            }
        }.map(SongItem::toMediaMetadata)
        addSongs(songs)
        addSongsToPlaylist(playlist.id, songs.map { it.id })
    }

    override suspend fun addMediaMetadataToPlaylist(playlist: PlaylistEntity, mediaMetadata: MediaMetadata) = withContext(IO) {
        addSong(mediaMetadata)
        addSongsToPlaylist(playlist.id, listOf(mediaMetadata.id))
    }

    override suspend fun refetchPlaylists(playlists: List<Playlist>): Unit = withContext(IO) {
        playlists.filter { playlist ->
            playlist.playlist.isYouTubePlaylist
        }.forEach { playlist ->
            val playlistPage = YouTube.browsePlaylist("VL${playlist.id}").getOrThrow()
            playlistDao.update(PlaylistEntity(
                id = playlistPage.playlist.id,
                name = playlistPage.playlist.title,
                author = playlistPage.playlist.author.name,
                authorId = playlistPage.playlist.author.id,
                thumbnailUrl = playlistPage.playlist.thumbnail,
                lastUpdateTime = LocalDateTime.now()
            ))
        }
    }

    override suspend fun downloadPlaylists(playlists: List<Playlist>) = withContext(IO) {
        downloadSongs(playlists
            .filter { it.playlist.isLocalPlaylist }
            .flatMap { getPlaylistSongs(it.id).first() }
            .map { it.id }
            .distinct())
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
    override fun getAllSearchHistory() = searchHistoryDao.getAllHistory()

    override fun getSearchHistory(query: String) = searchHistoryDao.getHistory(query)

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
    override fun getSongFormat(songId: String?) = formatDao.getSongFormatAsFlow(songId)

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

    override suspend fun deleteLyrics(songId: String) = lyricsDao.deleteLyrics(songId)

    companion object {
        const val PREVIEW_SIZE = 3
    }
}
