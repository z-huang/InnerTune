package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.zionhuang.innertube.utils.plus
import com.zionhuang.music.db.entities.ArtistHeader
import com.zionhuang.music.db.entities.LocalBaseItem
import com.zionhuang.music.db.entities.SongHeader
import com.zionhuang.music.models.ArtistSortInfoPreference
import com.zionhuang.music.models.SongSortInfoPreference
import com.zionhuang.music.models.SongSortType
import com.zionhuang.music.models.base.IMutableSortInfo
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("UNCHECKED_CAST")
class SongsViewModel(application: Application) : AndroidViewModel(application) {
    val songRepository = SongRepository
    val mediaSessionConnection = MediaSessionConnection

    val sortInfo: IMutableSortInfo<SongSortType> = SongSortInfoPreference

    var query: String? = null

    val allSongsFlow = SongSortInfoPreference.flow.flatMapLatest { sortInfo ->
        SongRepository.getAllSongs(sortInfo).flow
    }.map { list ->
        SongHeader(SongRepository.getSongCount(), SongSortInfoPreference.currentInfo) + list
    }

    val allArtistsFlow = ArtistSortInfoPreference.flow.flatMapLatest { sortInfo ->
        SongRepository.getAllArtists(sortInfo).flow
    }.map { list ->
        ArtistHeader(SongRepository.getArtistCount(), ArtistSortInfoPreference.currentInfo) + list
    }

    val allAlbumsFlow: Flow<PagingData<LocalBaseItem>> by lazy {
        Pager(PagingConfig(pageSize = 50)) {
            songRepository.getAllAlbums().pagingSource as PagingSource<Int, LocalBaseItem>
        }.flow.cachedIn(viewModelScope)
    }

    val allPlaylistsFlow: Flow<PagingData<LocalBaseItem>> by lazy {
        Pager(PagingConfig(pageSize = 50)) {
            songRepository.getAllPlaylists().pagingSource as PagingSource<Int, LocalBaseItem>
        }.flow.cachedIn(viewModelScope)
    }

    fun getArtistSongsAsFlow(artistId: String) = Pager(PagingConfig(pageSize = 50)) {
        songRepository.getArtistSongs(artistId, sortInfo).pagingSource as PagingSource<Int, LocalBaseItem>
    }.flow.cachedIn(viewModelScope)

    fun getPlaylistSongsAsFlow(playlistId: String) = Pager(PagingConfig(pageSize = 50)) {
        songRepository.getPlaylistSongs(playlistId, sortInfo).pagingSource
    }.flow.cachedIn(viewModelScope)
}
