package com.zionhuang.music.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.constants.PlaylistSongSortDescendingKey
import com.zionhuang.music.constants.PlaylistSongSortType
import com.zionhuang.music.constants.PlaylistSongSortTypeKey
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.extensions.reversed
import com.zionhuang.music.extensions.toEnum
import com.zionhuang.music.utils.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LocalPlaylistViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val playlistId = savedStateHandle.get<String>("playlistId")!!
    val playlist = database.playlist(playlistId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    val playlistSongs = combine(
        database.playlistSongs(playlistId),
        context.dataStore.data
            .map {
                it[PlaylistSongSortTypeKey].toEnum(PlaylistSongSortType.CUSTOM) to (it[PlaylistSongSortDescendingKey] ?: true)
            }
            .distinctUntilChanged()
    ) { songs, (sortType, sortDescending) ->
        when (sortType) {
            PlaylistSongSortType.CUSTOM -> songs
            PlaylistSongSortType.CREATE_DATE -> songs.sortedBy { it.map.id }
            PlaylistSongSortType.NAME -> songs.sortedBy { it.song.song.title }
            PlaylistSongSortType.ARTIST -> songs.sortedBy { song ->
                song.song.artists.joinToString { it.name }
            }
            PlaylistSongSortType.PLAY_TIME -> songs.sortedBy { it.song.song.totalPlayTime }
        }.reversed(sortDescending && sortType != PlaylistSongSortType.CUSTOM)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
