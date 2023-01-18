@file:OptIn(ExperimentalCoroutinesApi::class)

package com.zionhuang.music.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.YouTube
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.models.sortInfo.AlbumSortInfoPreference
import com.zionhuang.music.models.sortInfo.ArtistSortInfoPreference
import com.zionhuang.music.models.sortInfo.PlaylistSortInfoPreference
import com.zionhuang.music.models.sortInfo.SongSortInfoPreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class LibrarySongsViewModel @Inject constructor(
    database: MusicDatabase,
) : ViewModel() {
    val allSongs = SongSortInfoPreference.flow.flatMapLatest { sortInfo ->
        database.songs(sortInfo.type, sortInfo.isDescending)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

@HiltViewModel
class LibraryArtistsViewModel @Inject constructor(
    database: MusicDatabase,
) : ViewModel() {
    val allArtists = ArtistSortInfoPreference.flow.flatMapLatest { sortInfo ->
        database.artists(sortInfo.type, sortInfo.isDescending)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            allArtists.collect { artists ->
                artists
                    .map { it.artist }
                    .filter {
                        it.thumbnailUrl == null || Duration.between(it.lastUpdateTime, LocalDateTime.now()) > Duration.ofDays(10)
                    }
                    .forEach { artist ->
                        YouTube.browseArtist(artist.id).onSuccess { artistPage ->
                            database.query {
                                update(artist, artistPage)
                            }
                        }
                    }
            }
        }
    }
}

@HiltViewModel
class LibraryAlbumsViewModel @Inject constructor(
    database: MusicDatabase,
) : ViewModel() {
    val allAlbums = AlbumSortInfoPreference.flow.flatMapLatest { sortInfo ->
        database.albums(sortInfo.type, sortInfo.isDescending)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

@HiltViewModel
class LibraryPlaylistsViewModel @Inject constructor(
    database: MusicDatabase,
) : ViewModel() {
    val likedSongCount = database.likedSongsCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val downloadedSongCount = database.downloadedSongsCount()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val allPlaylists = PlaylistSortInfoPreference.flow.flatMapLatest { sortInfo ->
        database.playlists(sortInfo.type, sortInfo.isDescending)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

@HiltViewModel
class LikedSongsViewModel @Inject constructor(
    database: MusicDatabase,
) : ViewModel() {
    val songs = SongSortInfoPreference.flow.flatMapLatest { sortInfo ->
        database.likedSongs(sortInfo.type, sortInfo.isDescending)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

@HiltViewModel
class DownloadedSongsViewModel @Inject constructor(
    database: MusicDatabase,
) : ViewModel() {
    val songs = SongSortInfoPreference.flow.flatMapLatest { sortInfo ->
        database.downloadedSongs(sortInfo.type, sortInfo.isDescending)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
