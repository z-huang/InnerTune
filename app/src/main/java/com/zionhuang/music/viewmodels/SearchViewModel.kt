package com.zionhuang.music.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zionhuang.innertube.YouTube
import com.zionhuang.music.repos.YouTubeRepository

class SearchViewModel(
    private val repository: YouTubeRepository,
    private val query: String,
) : ViewModel() {
    val filter = MutableLiveData<YouTube.SearchFilter>(null)

    val pagingData = Pager(PagingConfig(pageSize = 20)) {
        filter.value.let {
            if (it == null) repository.searchAll(query)
            else repository.search(query, it)
        }
    }.flow.cachedIn(viewModelScope)
}

@Suppress("UNCHECKED_CAST")
class SearchViewModelFactory(
    private val repository: YouTubeRepository,
    private val query: String,
) : ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SearchViewModel(repository, query) as T
}
