package com.zionhuang.music.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.AlbumItem
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.db.entities.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val database: MusicDatabase,
) : ViewModel() {
    val isRefreshing = MutableStateFlow(false)

    val quickPicks = MutableStateFlow<List<Song>>(emptyList())
    val newReleaseAlbums = MutableStateFlow<List<AlbumItem>>(emptyList())

    private suspend fun load() {
        quickPicks.value = database.quickPicks().first().shuffled().take(20)
        YouTube.newReleaseAlbumsPreview().onSuccess {
            newReleaseAlbums.value = it
        }
    }

    fun refresh() {
        if (isRefreshing.value) return
        viewModelScope.launch(Dispatchers.IO) {
            isRefreshing.value = true
            load()
            isRefreshing.value = false
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            load()
        }
    }
}
