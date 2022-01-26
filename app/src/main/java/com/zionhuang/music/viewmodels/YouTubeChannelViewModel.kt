package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zionhuang.music.repos.YouTubeRepository

class YouTubeChannelViewModel(application: Application) : AndroidViewModel(application) {
    fun getChannel(channelId: String) = Pager(PagingConfig(pageSize = 20)) {
        YouTubeRepository.getChannel(channelId)
    }.flow.cachedIn(viewModelScope)
}