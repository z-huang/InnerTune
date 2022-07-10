package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zionhuang.innertube.YouTube
import com.zionhuang.music.repos.YouTubeRepository

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    val filter = MutableLiveData<YouTube.SearchFilter>(null)

    fun search(query: String) = Pager(PagingConfig(pageSize = 20)) {
        filter.value.let {
            if (it == null) YouTubeRepository.searchAll(query)
            else YouTubeRepository.search(query, it)
        }
    }.flow.cachedIn(viewModelScope)
}