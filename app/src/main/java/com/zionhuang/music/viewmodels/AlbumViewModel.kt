package com.zionhuang.music.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.zionhuang.innertube.YouTube
import com.zionhuang.music.db.entities.AlbumWithSongs
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.youtube.getAlbumWithSongs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlbumViewModel(
    context: Context,
    val albumId: String,
    val playlistId: String?,
) : ViewModel() {
    val albumWithSongs = MutableLiveData<AlbumWithSongs?>(null)

    init {
        viewModelScope.launch {
            delay(200)
            albumWithSongs.value = SongRepository(context).getAlbumWithSongs(albumId)
                ?: playlistId?.let {
                    YouTube.getAlbumWithSongs(context, albumId, it)
                }
        }
    }
}

@Suppress("UNCHECKED_CAST")
class AlbumViewModelFactory(
    val context: Context,
    val albumId: String,
    val playlistId: String?,
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        AlbumViewModel(context, albumId, playlistId) as T
}
