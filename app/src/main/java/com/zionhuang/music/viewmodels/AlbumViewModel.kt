package com.zionhuang.music.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.pages.AlbumPage
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.db.entities.AlbumWithSongs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlbumViewModel @Inject constructor(
    database: MusicDatabase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val albumId = savedStateHandle.get<String>("albumId")!!
    private val _viewState = MutableStateFlow<AlbumViewState?>(null)
    val viewState = _viewState.asStateFlow()
    val inLibrary: StateFlow<Boolean> = database.album(albumId)
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch {
            if (database.albumWithSongs(albumId).first() == null) {
                YouTube.album(albumId).getOrNull()?.let {
                    _viewState.value = AlbumViewState.Remote(it)
                }
            } else {
                database.albumWithSongs(albumId).collect { albumWithSongs ->
                    if (albumWithSongs != null) {
                        _viewState.value = AlbumViewState.Local(albumWithSongs)
                    }
                }
            }
        }
    }
}

sealed class AlbumViewState {
    data class Local(
        val albumWithSongs: AlbumWithSongs,
    ) : AlbumViewState()

    data class Remote(
        val albumPage: AlbumPage,
    ) : AlbumViewState()
}
