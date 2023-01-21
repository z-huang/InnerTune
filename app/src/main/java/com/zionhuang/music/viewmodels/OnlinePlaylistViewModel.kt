package com.zionhuang.music.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.PlaylistItem
import com.zionhuang.music.models.ItemsPage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnlinePlaylistViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val playlistId = savedStateHandle.get<String>("playlistId")!!

    val playlist = MutableStateFlow<PlaylistItem?>(null)
    val itemsPage = MutableStateFlow<ItemsPage?>(null)

    init {
        viewModelScope.launch {
            val playlistPage = YouTube.browsePlaylist(playlistId).getOrNull() ?: return@launch
            playlist.value = playlistPage.playlist
            itemsPage.value = ItemsPage(playlistPage.songs, playlistPage.songsContinuation)
        }
    }

    fun loadMore() {
        viewModelScope.launch {
            val oldItemsPage = itemsPage.value ?: return@launch
            val continuation = oldItemsPage.continuation ?: return@launch
            val playlistContinuationPage = YouTube.browsePlaylistContinuation(continuation).getOrNull() ?: return@launch
            itemsPage.update {
                ItemsPage(
                    items = (oldItemsPage.items + playlistContinuationPage.songs).distinctBy { it.id },
                    continuation = playlistContinuationPage.continuation
                )
            }
        }
    }
}
