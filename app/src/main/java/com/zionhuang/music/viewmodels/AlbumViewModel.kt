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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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

    init {
        viewModelScope.launch {
            _viewState.value = database.albumWithSongs(albumId).first()?.let {
                AlbumViewState.Local(it)
            } ?: YouTube.browseAlbum(albumId).getOrNull()?.let {
                AlbumViewState.Remote(it)
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
