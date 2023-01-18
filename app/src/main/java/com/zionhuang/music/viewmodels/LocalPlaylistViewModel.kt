package com.zionhuang.music.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.db.MusicDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LocalPlaylistViewModel @Inject constructor(
    database: MusicDatabase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val playlistId = savedStateHandle.get<String>("playlistId")!!
    val playlist = database.playlist(playlistId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    val playlistSongs = database.playlistSongs(playlistId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
