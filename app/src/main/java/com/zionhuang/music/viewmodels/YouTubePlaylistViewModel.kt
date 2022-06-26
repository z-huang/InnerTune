package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zionhuang.music.repos.NewPipeRepository
import com.zionhuang.music.youtube.NewPipeYouTubeHelper

class YouTubePlaylistViewModel(application: Application) : AndroidViewModel(application) {
    suspend fun getPlaylistInfo(playlistId: String) = NewPipeYouTubeHelper.getPlaylist(playlistId)

    fun getPlaylist(playlistId: String) = Pager(PagingConfig(pageSize = 20)) {
        NewPipeRepository.getPlaylist(playlistId)
    }.flow.cachedIn(viewModelScope)
}