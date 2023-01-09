package com.zionhuang.music.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zionhuang.innertube.YouTube
import com.zionhuang.music.repos.YouTubeRepository
import kotlinx.coroutines.flow.MutableStateFlow

class OnlineSearchViewModel(
    private val repository: YouTubeRepository,
    private val query: String,
) : ViewModel() {
    val filter = MutableStateFlow<YouTube.SearchFilter?>(null)

    val pagingData = Pager(PagingConfig(pageSize = 20)) {
        filter.value.let {
            if (it == null) repository.searchAll(query)
            else repository.search(query, it)
        }
    }.flow.cachedIn(viewModelScope)

    class Factory(
        private val repository: YouTubeRepository,
        private val query: String,
    ) : ViewModelProvider.NewInstanceFactory() {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = OnlineSearchViewModel(repository, query) as T
    }
}
