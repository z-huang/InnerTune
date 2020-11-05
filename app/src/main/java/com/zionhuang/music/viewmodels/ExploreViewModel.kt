package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zionhuang.music.youtube.YouTubeDataSource
import com.zionhuang.music.youtube.YouTubeRepository
import com.zionhuang.music.youtube.YouTubeRepository.Companion.getInstance

class ExploreViewModel(application: Application) : AndroidViewModel(application) {
    private val youTubeRepo: YouTubeRepository = getInstance(application)
    val flow = Pager(
            PagingConfig(pageSize = 20)
    ) { YouTubeDataSource.Popular(youTubeRepo) }.flow.cachedIn(viewModelScope)
}