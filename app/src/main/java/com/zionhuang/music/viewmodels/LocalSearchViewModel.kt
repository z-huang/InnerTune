package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import com.zionhuang.music.db.entities.LocalBaseItem
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.utils.livedata.SafeMutableLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class LocalSearchViewModel(application: Application) : AndroidViewModel(application) {
    val query = SafeMutableLiveData("")
    val filter = SafeMutableLiveData(Filter.ALL)
    val result: Flow<List<LocalBaseItem>> = query.asFlow().combine(filter.asFlow()) { query: String, filter: Filter ->
        query to filter
    }.flatMapLatest { (query, filter) ->
        if (query.isEmpty()) {
            emptyFlow()
        } else {
            when (filter) {
                Filter.ALL -> SongRepository.searchAll(query)
                Filter.SONG -> SongRepository.searchSongs(query)
                Filter.ALBUM -> SongRepository.searchAlbums(query)
                Filter.ARTIST -> SongRepository.searchArtists(query)
                Filter.PLAYLIST -> SongRepository.searchPlaylists(query)
            }
        }
    }

    enum class Filter {
        ALL, SONG, ALBUM, ARTIST, PLAYLIST
    }
}