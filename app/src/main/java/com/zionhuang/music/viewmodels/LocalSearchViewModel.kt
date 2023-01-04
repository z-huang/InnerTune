package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class LocalSearchViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = SongRepository(application)
    val query = MutableStateFlow("")
    val filter = MutableStateFlow(LocalFilter.ALL)
    val result = combine(query, filter) { query, filter ->
        query to filter
    }.flatMapLatest { (query, filter) ->
        if (query.isEmpty()) {
            flowOf(LocalSearchResult("", filter, emptyMap()))
        } else {
            when (filter) {
                LocalFilter.ALL -> songRepository.searchAll(query)
                LocalFilter.SONG -> songRepository.searchSongs(query)
                LocalFilter.ALBUM -> songRepository.searchAlbums(query)
                LocalFilter.ARTIST -> songRepository.searchArtists(query)
                LocalFilter.PLAYLIST -> songRepository.searchPlaylists(query)
            }.map { list ->
                LocalSearchResult(
                    query = query,
                    filter = filter,
                    map = list.groupBy {
                        when (it) {
                            is Song -> LocalFilter.SONG
                            is Album -> LocalFilter.ALBUM
                            is Artist -> LocalFilter.ARTIST
                            is Playlist -> LocalFilter.PLAYLIST
                        }
                    })
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, LocalSearchResult("", filter.value, emptyMap()))
}

enum class LocalFilter {
    ALL, SONG, ALBUM, ARTIST, PLAYLIST
}

data class LocalSearchResult(
    val query: String,
    val filter: LocalFilter,
    val map: Map<LocalFilter, List<LocalItem>>,
)