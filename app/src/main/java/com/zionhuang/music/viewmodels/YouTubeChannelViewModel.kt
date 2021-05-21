package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zionhuang.music.youtube.YouTubeDataSource

class YouTubeChannelViewModel(application: Application) : AndroidViewModel(application) {
    fun getChannel(url: String) = Pager(PagingConfig(pageSize = 20)) {
        YouTubeDataSource.NewPipeChannel(url)
    }.flow.cachedIn(viewModelScope)
}