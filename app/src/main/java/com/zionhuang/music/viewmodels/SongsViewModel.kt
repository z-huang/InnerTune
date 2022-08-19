package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.zionhuang.music.db.entities.LocalItem
import com.zionhuang.music.models.PreferenceSortInfo
import com.zionhuang.music.models.base.IMutableSortInfo
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.repos.base.LocalRepository
import kotlinx.coroutines.flow.Flow

@Suppress("UNCHECKED_CAST")
class SongsViewModel(application: Application) : AndroidViewModel(application) {
    val songRepository: LocalRepository = SongRepository
    val mediaSessionConnection = MediaSessionConnection

    val sortInfo: IMutableSortInfo = PreferenceSortInfo

    var query: String? = null

    val allSongsFlow: Flow<PagingData<LocalItem>> by lazy {
        Pager(PagingConfig(pageSize = 50, enablePlaceholders = true)) {
            songRepository.getAllSongs(sortInfo).pagingSource as PagingSource<Int, LocalItem>
        }.flow.cachedIn(viewModelScope)
    }

    val allArtistsFlow: Flow<PagingData<LocalItem>> by lazy {
        Pager(PagingConfig(pageSize = 50)) {
            songRepository.getAllArtists().pagingSource as PagingSource<Int, LocalItem>
        }.flow.cachedIn(viewModelScope)
    }

    val allAlbumsFlow: Flow<PagingData<LocalItem>> by lazy {
        Pager(PagingConfig(pageSize = 50)) {
            songRepository.getAllAlbums().pagingSource as PagingSource<Int, LocalItem>
        }.flow.cachedIn(viewModelScope)
    }

    val allPlaylistsFlow: Flow<PagingData<LocalItem>> by lazy {
        Pager(PagingConfig(pageSize = 50)) {
            songRepository.getAllPlaylists().pagingSource as PagingSource<Int, LocalItem>
        }.flow.cachedIn(viewModelScope)
    }

    fun getArtistSongsAsFlow(artistId: String) = Pager(PagingConfig(pageSize = 50)) {
        songRepository.getArtistSongs(artistId, sortInfo).pagingSource as PagingSource<Int, LocalItem>
    }.flow.cachedIn(viewModelScope)

    fun getPlaylistSongsAsFlow(playlistId: String) = Pager(PagingConfig(pageSize = 50)) {
        songRepository.getPlaylistSongs(playlistId, sortInfo).pagingSource
    }.flow.cachedIn(viewModelScope)
}
