package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zionhuang.music.youtube.YouTubeDataSource

class YouTubePlaylistViewModel(application: Application) : AndroidViewModel(application) {
    fun getPlaylist(url: String) = Pager(PagingConfig(pageSize = 20)) {
        YouTubeDataSource.NewPipePlaylist(url)
    }.flow.cachedIn(viewModelScope)
}