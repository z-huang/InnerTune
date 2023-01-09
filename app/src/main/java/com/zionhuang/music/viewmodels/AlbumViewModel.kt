package com.zionhuang.music.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.YouTube
import com.zionhuang.music.db.entities.AlbumWithSongs
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.youtube.getAlbumWithSongs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class AlbumViewModel(
    context: Context,
    albumId: String,
    playlistId: String?,
) : ViewModel() {
    val albumWithSongs = MutableStateFlow<AlbumWithSongs?>(null)

    init {
        viewModelScope.launch {
            albumWithSongs.value = SongRepository(context).getAlbumWithSongs(albumId)
                ?: YouTube.getAlbumWithSongs(context, albumId, playlistId)
        }
    }

    class Factory(
        val context: Context,
        val albumId: String,
        val playlistId: String?,
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AlbumViewModel(context, albumId, playlistId) as T
    }
}
