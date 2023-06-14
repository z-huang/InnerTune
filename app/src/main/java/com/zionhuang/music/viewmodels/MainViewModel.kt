package com.zionhuang.music.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.db.MusicDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    database: MusicDatabase,
) : ViewModel() {
    val librarySongIds = database.allSongId()
        .map(List<String>::toHashSet)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val likedSongIds = database.allLikedSongId()
        .map(List<String>::toHashSet)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val libraryAlbumIds = database.allAlbumId()
        .map(List<String>::toHashSet)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val libraryPlaylistIds = database.allPlaylistId()
        .map(List<String>::toHashSet)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())
}