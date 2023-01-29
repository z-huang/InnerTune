package com.zionhuang.music.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.PlaylistItem
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.models.ItemsPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlinePlaylistViewModel @Inject constructor(
    database: MusicDatabase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val playlistId = savedStateHandle.get<String>("playlistId")!!

    val playlist = MutableStateFlow<PlaylistItem?>(null)
    val itemsPage = MutableStateFlow<ItemsPage?>(null)
    val inLibrary = database.playlist(playlistId)
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch {
            val playlistPage = YouTube.playlist(playlistId).getOrNull() ?: return@launch
            playlist.value = playlistPage.playlist
            itemsPage.value = ItemsPage(playlistPage.songs, playlistPage.songsContinuation)
        }
    }

    fun loadMore() {
        viewModelScope.launch {
            val oldItemsPage = itemsPage.value ?: return@launch
            val continuation = oldItemsPage.continuation ?: return@launch
            val playlistContinuationPage = YouTube.playlistContinuation(continuation).getOrNull() ?: return@launch
            itemsPage.update {
                ItemsPage(
                    items = (oldItemsPage.items + playlistContinuationPage.songs).distinctBy { it.id },
                    continuation = playlistContinuationPage.continuation
                )
            }
        }
    }
}
