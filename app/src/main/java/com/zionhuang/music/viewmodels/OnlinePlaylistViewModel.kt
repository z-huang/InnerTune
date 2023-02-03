package com.zionhuang.music.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.PlaylistItem
import com.zionhuang.innertube.models.SongItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlinePlaylistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val playlistId = savedStateHandle.get<String>("playlistId")!!

    val playlist = MutableStateFlow<PlaylistItem?>(null)
    val playlistSongs = MutableStateFlow<List<SongItem>>(emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val playlistPage = YouTube.playlist(playlistId).getOrNull() ?: return@launch
            val songs = playlistPage.songs.toMutableList()
            var continuation = playlistPage.songsContinuation
            while (continuation != null) {
                val continuationPage = YouTube.playlistContinuation(continuation).getOrNull() ?: break
                songs += continuationPage.songs
                continuation = continuationPage.continuation
            }
            playlist.value = playlistPage.playlist
            playlistSongs.value = songs
        }
    }
}
