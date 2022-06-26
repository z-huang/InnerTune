package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import com.zionhuang.music.repos.NewPipeRepository
import com.zionhuang.music.utils.livedata.SafeMutableLiveData
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_SONGS

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    var searchFilter = SafeMutableLiveData(MUSIC_SONGS)

    fun search(query: String) = Pager(PagingConfig(pageSize = 20)) {
        NewPipeRepository.search(query, searchFilter.value)
    }.flow.cachedIn(viewModelScope)
}