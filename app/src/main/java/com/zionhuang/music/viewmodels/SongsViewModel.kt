package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import com.zionhuang.innertube.utils.plus
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.models.sortInfo.AlbumSortInfoPreference
import com.zionhuang.music.models.sortInfo.ArtistSortInfoPreference
import com.zionhuang.music.models.sortInfo.PlaylistSortInfoPreference
import com.zionhuang.music.models.sortInfo.SongSortInfoPreference
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class SongsViewModel(application: Application) : AndroidViewModel(application) {
    val songRepository = SongRepository
    val mediaSessionConnection = MediaSessionConnection

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

    val allAlbumsFlow = AlbumSortInfoPreference.flow.flatMapLatest { sortInfo ->
        SongRepository.getAllAlbums(sortInfo).flow
    }.map { list ->
        AlbumHeader(SongRepository.getAlbumCount(), AlbumSortInfoPreference.currentInfo) + list
    }

    val allPlaylistsFlow = PlaylistSortInfoPreference.flow.flatMapLatest { sortInfo ->
        SongRepository.getAllPlaylists(sortInfo).flow
    }.map { list ->
        PlaylistHeader(SongRepository.getPlaylistCount(), PlaylistSortInfoPreference.currentInfo) + list
    }

    @Suppress("UNCHECKED_CAST")
    fun getArtistSongsAsFlow(artistId: String) = Pager(PagingConfig(pageSize = 50)) {
        songRepository.getArtistSongs(artistId, SongSortInfoPreference).pagingSource as PagingSource<Int, LocalBaseItem>
    }.flow.cachedIn(viewModelScope)

    fun getPlaylistSongsAsFlow(playlistId: String) = Pager(PagingConfig(pageSize = 50)) {
        songRepository.getPlaylistSongs(playlistId, SongSortInfoPreference).pagingSource
    }.flow.cachedIn(viewModelScope)
}
