package com.zionhuang.music.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.innertube.YouTube
import com.zionhuang.music.constants.StatPeriod
import com.zionhuang.music.db.MusicDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    val database: MusicDatabase,
) : ViewModel() {
    val statPeriod = MutableStateFlow(StatPeriod.`1_WEEK`)

    val mostPlayedSongs = statPeriod.flatMapLatest { period ->
        database.mostPlayedSongs(period.toTimeMillis())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val mostPlayedArtists = statPeriod.flatMapLatest { period ->
        database.mostPlayedArtists(period.toTimeMillis())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    val mostPlayedAlbums = statPeriod.flatMapLatest { period ->
        database.mostPlayedAlbums(period.toTimeMillis())
    }.map { albums ->
        albums.mapNotNull { id ->
            YouTube.album(id, withSongs = false).getOrNull()?.album
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            mostPlayedArtists.collect { artists ->
                artists
                    .map { it.artist }
                    .filter {
                        it.thumbnailUrl == null || Duration.between(it.lastUpdateTime, LocalDateTime.now()) > Duration.ofDays(10)
                    }
                    .forEach { artist ->
                        YouTube.artist(artist.id).onSuccess { artistPage ->
                            database.query {
                                update(artist, artistPage)
                            }
                        }
                    }
            }
        }
    }
}
