package com.zionhuang.music.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class LocalPlaylistViewModel(
    context: Context,
    playlistId: String,
) : ViewModel() {
    val playlist = SongRepository(context).getPlaylist(playlistId).stateIn(viewModelScope, SharingStarted.Lazily, null)
    val playlistSongs = SongRepository(context).getPlaylistSongs(playlistId).flow.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

@Suppress("UNCHECKED_CAST")
class LocalPlaylistViewModelFactory(
    val context: Context,
    val playlistId: String,
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        LocalPlaylistViewModel(context, playlistId) as T
}
