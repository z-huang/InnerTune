package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val songRepository = SongRepository(application)

    val librarySongIds = songRepository.getAllSongId()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val libraryAlbumIds = songRepository.getAllAlbumId()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val libraryPlaylistIds = songRepository.getAllPlaylistId()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}