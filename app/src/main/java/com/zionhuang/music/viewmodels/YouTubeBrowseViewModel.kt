package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zionhuang.innertube.models.BrowseEndpoint
import com.zionhuang.music.repos.YouTubeRepository

class YouTubeBrowseViewModel(application: Application, private val browseEndpoint: BrowseEndpoint) : AndroidViewModel(application) {
    val pagingData = Pager(PagingConfig(pageSize = 20)) {
        YouTubeRepository.browse(browseEndpoint)
    }.flow.cachedIn(viewModelScope)
}

class YouTubeBrowseViewModelFactory(
    val application: Application,
    private val browseEndpoint: BrowseEndpoint,
) : ViewModelProvider.AndroidViewModelFactory(application) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
        YouTubeBrowseViewModel(application, browseEndpoint) as T
}