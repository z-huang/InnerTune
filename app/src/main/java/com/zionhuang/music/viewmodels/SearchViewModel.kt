package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.TerminalSeparatorType.FULLY_COMPLETE
import androidx.paging.cachedIn
import androidx.paging.insertHeaderItem
import com.zionhuang.music.models.HeaderInfoItem
import com.zionhuang.music.repos.YouTubeRepository
import kotlinx.coroutines.flow.map
import org.schabi.newpipe.extractor.services.youtube.linkHandler.YoutubeSearchQueryHandlerFactory.MUSIC_SONGS

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    var filter: String = MUSIC_SONGS

    fun search(query: String) = Pager(PagingConfig(pageSize = 20)) {
        YouTubeRepository.search(query, filter)
    }.flow.map { pagingData ->
        pagingData.insertHeaderItem(FULLY_COMPLETE, HEADER_ITEM)
    }.cachedIn(viewModelScope)

    companion object {
        private val HEADER_ITEM = HeaderInfoItem()
    }
}