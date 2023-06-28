package com.zionhuang.music.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.db.MusicDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    val database: MusicDatabase,
) : ViewModel() {
    val mostPlayedSongs = database.mostPlayedSongs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val mostPlayedArtists = database.mostPlayerArtists()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}