package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.zionhuang.music.db.SongEntity
import com.zionhuang.music.db.SongRepository
import kotlinx.coroutines.flow.Flow

class SongsViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository: SongRepository = SongRepository(application)
    val allSongsFlow: Flow<PagingData<SongEntity>> = Pager(
            PagingConfig(pageSize = 50)
    ) { songRepository.getAllSongsAsPagingSource() }.flow.cachedIn(viewModelScope)
}