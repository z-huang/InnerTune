package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zionhuang.innertube.YouTube
import com.zionhuang.music.repos.YouTubeRepository

class YouTubeSearchViewModel(application: Application, query: String) : AndroidViewModel(application) {
    private val youTubeRepository = YouTubeRepository(application)
    val filter = MutableLiveData<YouTube.SearchFilter>(null)

    val pagingData = Pager(PagingConfig(pageSize = 20)) {
        filter.value.let {
            if (it == null) youTubeRepository.searchAll(query)
            else youTubeRepository.search(query, it)
        }
    }.flow.cachedIn(viewModelScope)
}

class YouTubeSearchViewModelFactory(val application: Application, val query: String) : ViewModelProvider.AndroidViewModelFactory(application) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
        YouTubeSearchViewModel(application, query) as T
}