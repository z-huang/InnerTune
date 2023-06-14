package com.zionhuang.music.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.constants.SongSortDescendingKey
import com.zionhuang.music.constants.SongSortType
import com.zionhuang.music.constants.SongSortTypeKey
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.DOWNLOADED_PLAYLIST_ID
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.LIKED_PLAYLIST_ID
import com.zionhuang.music.extensions.toEnum
import com.zionhuang.music.utils.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class BuiltInPlaylistViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val playlistId = savedStateHandle.get<String>("playlistId")!!

    @OptIn(ExperimentalCoroutinesApi::class)
    val songs = context.dataStore.data
        .map {
            it[SongSortTypeKey].toEnum(SongSortType.CREATE_DATE) to (it[SongSortDescendingKey] ?: true)
        }
        .distinctUntilChanged()
        .flatMapLatest { (sortType, descending) ->
            when (playlistId) {
                LIKED_PLAYLIST_ID -> database.likedSongs(sortType, descending)
                DOWNLOADED_PLAYLIST_ID -> database.downloadedSongs(sortType, descending)
                else -> error("Unknown playlist id")
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
