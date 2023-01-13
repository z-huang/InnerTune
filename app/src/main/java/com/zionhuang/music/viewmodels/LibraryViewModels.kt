@file:OptIn(ExperimentalCoroutinesApi::class)

package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.models.sortInfo.AlbumSortInfoPreference
import com.zionhuang.music.models.sortInfo.ArtistSortInfoPreference
import com.zionhuang.music.models.sortInfo.PlaylistSortInfoPreference
import com.zionhuang.music.models.sortInfo.SongSortInfoPreference
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class LibrarySongsViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = SongRepository(application)

    val allSongs = SongSortInfoPreference.flow.flatMapLatest { sortInfo ->
        songRepository.getAllSongs(sortInfo)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

class LibraryArtistsViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = SongRepository(application)

    val allArtists = ArtistSortInfoPreference.flow.flatMapLatest { sortInfo ->
        songRepository.getAllArtists(sortInfo)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

class LibraryAlbumsViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = SongRepository(application)

    val allAlbums = AlbumSortInfoPreference.flow.flatMapLatest { sortInfo ->
        songRepository.getAllAlbums(sortInfo)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

class LibraryPlaylistsViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = SongRepository(application)

    val likedSongCount = songRepository.getLikedSongCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val downloadedSongCount = songRepository.getDownloadedSongCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val allPlaylists = PlaylistSortInfoPreference.flow.flatMapLatest { sortInfo ->
        songRepository.getAllPlaylists(sortInfo)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

class LikedSongsViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = SongRepository(application)

    val songs = SongSortInfoPreference.flow.flatMapLatest { sortInfo ->
        songRepository.getLikedSongs(sortInfo)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

class DownloadedSongsViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = SongRepository(application)

    val songs = SongSortInfoPreference.flow.flatMapLatest { sortInfo ->
        songRepository.getDownloadedSongs(sortInfo)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
