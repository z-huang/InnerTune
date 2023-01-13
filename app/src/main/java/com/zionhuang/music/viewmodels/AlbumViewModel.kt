package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.pages.AlbumPage
import com.zionhuang.music.db.entities.AlbumWithSongs
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlbumViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle,
) : AndroidViewModel(application) {
    val songRepository = SongRepository(application)
    val albumId = savedStateHandle.get<String>("albumId")!!
    private val _viewState = MutableStateFlow<AlbumViewState?>(null)
    val viewState = _viewState.asStateFlow()

    init {
        viewModelScope.launch {
            _viewState.value = songRepository.getAlbumWithSongs(albumId)?.let {
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
