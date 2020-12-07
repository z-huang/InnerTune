package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.api.services.youtube.model.SearchResult
import com.zionhuang.music.db.SongEntity
import com.zionhuang.music.repository.SongRepository
import com.zionhuang.music.youtube.YouTubeDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SongsViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository: SongRepository = SongRepository(application)
    val allSongsFlow: Flow<PagingData<SongEntity>> = Pager(
            PagingConfig(pageSize = 50)
    ) { songRepository.getAllSongsAsPagingSource() }.flow.cachedIn(viewModelScope)
}