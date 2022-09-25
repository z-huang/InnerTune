package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.models.sortInfo.AlbumSortInfoPreference
import com.zionhuang.music.models.sortInfo.ArtistSortInfoPreference
import com.zionhuang.music.models.sortInfo.PlaylistSortInfoPreference
import com.zionhuang.music.models.sortInfo.SongSortInfoPreference
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class SongsViewModel(application: Application) : AndroidViewModel(application) {
    val allSongsFlow = SongSortInfoPreference.flow.flatMapLatest { sortInfo ->
        SongRepository.getAllSongs(sortInfo).flow
    }.map { list ->
        listOf(SongHeader(SongRepository.getSongCount(), SongSortInfoPreference.currentInfo)) + list
    }

    val allArtistsFlow = ArtistSortInfoPreference.flow.flatMapLatest { sortInfo ->
        SongRepository.getAllArtists(sortInfo).flow
    }.map { list ->
        listOf(ArtistHeader(SongRepository.getArtistCount(), ArtistSortInfoPreference.currentInfo)) + list
    }

    val allAlbumsFlow = AlbumSortInfoPreference.flow.flatMapLatest { sortInfo ->
        SongRepository.getAllAlbums(sortInfo).flow
    }.map { list ->
        listOf(AlbumHeader(SongRepository.getAlbumCount(), AlbumSortInfoPreference.currentInfo)) + list
    }

    val allPlaylistsFlow = combine(
        SongRepository.getLikedSongCount().map { LikedPlaylist(it) },
        SongRepository.getDownloadedSongCount().map { DownloadedPlaylist(it) },
        PlaylistSortInfoPreference.flow.flatMapLatest { sortInfo ->
            SongRepository.getAllPlaylists(sortInfo).flow
        }
    ) { likedPlaylist, downloadedPlaylist, playlists ->
        listOf(
            PlaylistHeader(playlists.size + 2, PlaylistSortInfoPreference.currentInfo),
            likedPlaylist,
            downloadedPlaylist
        ) + playlists
    }

    fun getArtistSongsAsFlow(artistId: String) = SongSortInfoPreference.flow.flatMapLatest { sortInfo ->
        SongRepository.getArtistSongs(artistId, sortInfo).flow
    }.map { list ->
        listOf(SongHeader(SongRepository.getArtistSongCount(artistId), SongSortInfoPreference.currentInfo)) + list
    }

    fun getPlaylistSongsAsFlow(playlistId: String) = SongRepository.getPlaylistSongs(playlistId).flow.map { list ->
        listOf(PlaylistSongHeader(list.size, list.sumOf { it.song.duration.toLong() })) + list
    }

    fun getLikedSongsAsFlow() = SongSortInfoPreference.flow.flatMapLatest { sortInfo ->
        SongRepository.getLikedSongs(sortInfo).flow
    }.map { list ->
        listOf(SongHeader(list.size, SongSortInfoPreference.currentInfo)) + list
    }

    fun getDownloadedSongsAsFlow() = SongSortInfoPreference.flow.flatMapLatest { sortInfo ->
        SongRepository.getDownloadedSongs(sortInfo).flow
    }.map { list ->
        listOf(SongHeader(list.size, SongSortInfoPreference.currentInfo)) + list
    }
}
