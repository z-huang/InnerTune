package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.music.repos.YouTubeRepository

class YouTubeViewModel(application: Application) : AndroidViewModel(application) {
    fun browse(endpoint: BrowseEndpoint) = Pager(PagingConfig(pageSize = 20)) {
        YouTubeRepository.browse(endpoint)
    }.flow.cachedIn(viewModelScope)
}