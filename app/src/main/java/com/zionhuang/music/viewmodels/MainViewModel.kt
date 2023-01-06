package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val songRepository = SongRepository(application)

    val librarySongIds = songRepository.getAllSongId()
        .map(List<String>::toHashSet)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val likedSongIds = songRepository.getAllLikedSongId()
        .map(List<String>::toHashSet)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val libraryAlbumIds = songRepository.getAllAlbumId()
        .map(List<String>::toHashSet)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())

    val libraryPlaylistIds = songRepository.getAllPlaylistId()
        .map(List<String>::toHashSet)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptySet())
}