package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.models.sortInfo.AlbumSortInfoPreference
import com.zionhuang.music.models.sortInfo.ArtistSortInfoPreference
import com.zionhuang.music.models.sortInfo.PlaylistSortInfoPreference
import com.zionhuang.music.models.sortInfo.SongSortInfoPreference
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class SongsViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = SongRepository(application)

    val allSongsFlow = SongSortInfoPreference.flow.flatMapLatest { sortInfo ->
        songRepository.getAllSongs(sortInfo).flow
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allArtistsFlow = ArtistSortInfoPreference.flow.flatMapLatest { sortInfo ->
        songRepository.getAllArtists(sortInfo).flow
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allAlbumsFlow = AlbumSortInfoPreference.flow.flatMapLatest { sortInfo ->
        songRepository.getAllAlbums(sortInfo).flow
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allPlaylistsFlow = PlaylistSortInfoPreference.flow.flatMapLatest { sortInfo ->
        songRepository.getAllPlaylists(sortInfo).flow
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val likedSongCount = songRepository.getLikedSongCount()
    val downloadedSongCount = songRepository.getDownloadedSongCount()

    fun getArtistSongsAsFlow(artistId: String) = SongSortInfoPreference.flow.flatMapLatest { sortInfo ->
        songRepository.getArtistSongs(artistId, sortInfo).flow
    }.map { list ->
        listOf(SongHeader(songRepository.getArtistSongCount(artistId), SongSortInfoPreference.currentInfo)) + list
    }

    fun getPlaylistSongsAsFlow(playlistId: String) = songRepository.getPlaylistSongs(playlistId).flow.map { list ->
        listOf(PlaylistSongHeader(list.size, list.sumOf { it.song.duration.toLong() })) + list
    }

    fun getLikedSongsAsFlow() = SongSortInfoPreference.flow.flatMapLatest { sortInfo ->
        songRepository.getLikedSongs(sortInfo).flow
    }.map { list ->
        listOf(SongHeader(list.size, SongSortInfoPreference.currentInfo)) + list
    }

    fun getDownloadedSongsAsFlow() = SongSortInfoPreference.flow.flatMapLatest { sortInfo ->
        songRepository.getDownloadedSongs(sortInfo).flow
    }.map { list ->
        listOf(SongHeader(list.size, SongSortInfoPreference.currentInfo)) + list
    }
}
