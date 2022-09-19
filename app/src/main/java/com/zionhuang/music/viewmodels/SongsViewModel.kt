package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.zionhuang.innertube.utils.plus
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.models.sortInfo.AlbumSortInfoPreference
import com.zionhuang.music.models.sortInfo.ArtistSortInfoPreference
import com.zionhuang.music.models.sortInfo.PlaylistSortInfoPreference
import com.zionhuang.music.models.sortInfo.SongSortInfoPreference
import com.zionhuang.music.playback.MediaSessionConnection
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class SongsViewModel(application: Application) : AndroidViewModel(application) {
    val songRepository = SongRepository
    val mediaSessionConnection = MediaSessionConnection

    var query: String? = null

    val allSongsFlow = SongSortInfoPreference.flow.flatMapLatest { sortInfo ->
        SongRepository.getAllSongs(sortInfo).flow
    }.map { list ->
        SongHeader(SongRepository.getSongCount(), SongSortInfoPreference.currentInfo) + list
    }

    val allArtistsFlow = ArtistSortInfoPreference.flow.flatMapLatest { sortInfo ->
        SongRepository.getAllArtists(sortInfo).flow
    }.map { list ->
        ArtistHeader(SongRepository.getArtistCount(), ArtistSortInfoPreference.currentInfo) + list
    }

    val allAlbumsFlow = AlbumSortInfoPreference.flow.flatMapLatest { sortInfo ->
        SongRepository.getAllAlbums(sortInfo).flow
    }.map { list ->
        AlbumHeader(SongRepository.getAlbumCount(), AlbumSortInfoPreference.currentInfo) + list
    }

    val allPlaylistsFlow = PlaylistSortInfoPreference.flow.flatMapLatest { sortInfo ->
        SongRepository.getAllPlaylists(sortInfo).flow
    }.map { list ->
        PlaylistHeader(SongRepository.getPlaylistCount(), PlaylistSortInfoPreference.currentInfo) + list
    }

    @Suppress("UNCHECKED_CAST")
    fun getArtistSongsAsFlow(artistId: String) = SongSortInfoPreference.flow.flatMapLatest { sortInfo ->
        SongRepository.getArtistSongs(artistId, sortInfo).flow
    }.map { list ->
        SongHeader(SongRepository.getArtistSongCount(artistId), SongSortInfoPreference.currentInfo) + list
    }

    fun getPlaylistSongsAsFlow(playlistId: String) = songRepository.getPlaylistSongs(playlistId).flow.map { list ->
        PlaylistSongHeader(list.size, list.sumOf { it.song.duration.toLong() }) + list
    }
}
