package com.zionhuang.music.repos

import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.db.daos.ArtistDao
import com.zionhuang.music.db.daos.DownloadDao
import com.zionhuang.music.db.daos.PlaylistDao
import com.zionhuang.music.db.daos.SongDao
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.models.SortInfo
import com.zionhuang.music.extensions.getApplication
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext

object DBRepository : LocalRepository {
    private val context = getApplication()
    private val musicDatabase = MusicDatabase.getInstance()
    private val songDao: SongDao = musicDatabase.songDao
    private val artistDao: ArtistDao = musicDatabase.artistDao
    private val playlistDao: PlaylistDao = musicDatabase.playlistDao
    private val downloadDao: DownloadDao = musicDatabase.downloadDao

    override fun searchSongs(query: String) = ListWrapper(
        pagingSource = { songDao.searchSongs(query) }
    )

    override suspend fun getSongById(songId: String): Song? = withContext(IO) { songDao.getSongById(songId) }

    override fun getArtistSongs(artistId: Int, sortInfo: SortInfo): ListWrapper<Int, Song> = ListWrapper(
        list = { songDao.getArtistSongsAsList(artistId, sortInfo) },
        pagingSource = { songDao.getArtistSongsAsPagingSource(artistId, sortInfo) }
    )

    override fun getPlaylistSongs(playlistId: Int, sortInfo: SortInfo): ListWrapper<Int, Song> = ListWrapper(
        list = { songDao.getPlaylistSongsAsList(playlistId) },
        pagingSource = { songDao.getPlaylistSongsAsPagingSource(playlistId) }
    )

    override fun searchArtists(query: String) = ListWrapper<Int, ArtistEntity>(
        list = { artistDao.searchArtists(query) }
    )

    override suspend fun getArtistById(artistId: Int): ArtistEntity? = withContext(IO) { artistDao.getArtist(artistId) }

    override fun searchPlaylists(query: String): ListWrapper<Int, PlaylistEntity> {
        TODO("Not yet implemented")
    }

    override suspend fun getPlaylistById(playlistId: Int): PlaylistEntity? {
        TODO("Not yet implemented")
    }
}