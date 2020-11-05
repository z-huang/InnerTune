package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.api.services.youtube.model.SearchResult
import com.zionhuang.music.youtube.YouTubeDataSource
import com.zionhuang.music.youtube.YouTubeRepository
import com.zionhuang.music.youtube.YouTubeRepository.Companion.getInstance
import kotlinx.coroutines.flow.Flow

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val youtubeRepo: YouTubeRepository = getInstance(application)
    fun search(query: String): Flow<PagingData<SearchResult>> = Pager(
            PagingConfig(pageSize = 20)
    ) { YouTubeDataSource.Search(youtubeRepo, query) }.flow.cachedIn(viewModelScope)

}