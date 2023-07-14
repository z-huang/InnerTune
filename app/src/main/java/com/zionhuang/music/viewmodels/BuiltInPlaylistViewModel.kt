package com.zionhuang.music.viewmodels

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.offline.Download.STATE_COMPLETED
import com.zionhuang.music.constants.DownloadedSongSortDescendingKey
import com.zionhuang.music.constants.DownloadedSongSortType
import com.zionhuang.music.constants.DownloadedSongSortTypeKey
import com.zionhuang.music.constants.SongSortDescendingKey
import com.zionhuang.music.constants.SongSortType
import com.zionhuang.music.constants.SongSortTypeKey
import com.zionhuang.music.db.MusicDatabase
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.DOWNLOADED_PLAYLIST_ID
import com.zionhuang.music.db.entities.PlaylistEntity.Companion.LIKED_PLAYLIST_ID
import com.zionhuang.music.extensions.reversed
import com.zionhuang.music.extensions.toEnum
import com.zionhuang.music.playback.DownloadUtil
import com.zionhuang.music.utils.dataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BuiltInPlaylistViewModel @Inject constructor(
    @ApplicationContext context: Context,
    database: MusicDatabase,
    downloadUtil: DownloadUtil,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    val playlistId = savedStateHandle.get<String>("playlistId")!!

    @OptIn(ExperimentalCoroutinesApi::class)
    val songs = when (playlistId) {
        LIKED_PLAYLIST_ID -> context.dataStore.data
            .map {
                it[SongSortTypeKey].toEnum(SongSortType.CREATE_DATE) to (it[SongSortDescendingKey] ?: true)
            }
            .distinctUntilChanged()
            .flatMapLatest { (sortType, descending) ->
                database.likedSongs(sortType, descending)
            }

        DOWNLOADED_PLAYLIST_ID -> combine(
            downloadUtil.downloads.flatMapLatest { downloads ->
                database.songs(
                    downloads.filter { (_, download) ->
                        download.state == STATE_COMPLETED
                    }.keys.toList()
                ).map { songs ->
                    songs.map { it to downloads[it.id] }
                }
            },
            context.dataStore.data
                .map {
                    it[DownloadedSongSortTypeKey].toEnum(DownloadedSongSortType.CREATE_DATE) to (it[DownloadedSongSortDescendingKey] ?: true)
                }
                .distinctUntilChanged()
        ) { songs, (sortType, descending) ->
            when (sortType) {
                DownloadedSongSortType.CREATE_DATE -> songs.sortedBy { it.second?.updateTimeMs ?: 0L }
                DownloadedSongSortType.NAME -> songs.sortedBy { it.first.song.title }
                DownloadedSongSortType.ARTIST -> songs.sortedBy { song ->
                    song.first.artists.joinToString(separator = "") { it.name }
                }
                DownloadedSongSortType.PLAY_TIME -> songs.sortedBy { it.first.song.totalPlayTime }
            }
                .map { it.first }
                .reversed(descending)
        }

        else -> error("Unknown playlist id")
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
