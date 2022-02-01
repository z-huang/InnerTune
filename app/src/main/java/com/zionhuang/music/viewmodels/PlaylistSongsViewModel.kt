package com.zionhuang.music.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.extensions.swap
import com.zionhuang.music.repos.SongRepository
import kotlinx.coroutines.launch

class PlaylistSongsViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = SongRepository

    fun processMove(playlistId: Int, moves: List<Pair<Int, Int>>) {
        viewModelScope.launch {
            val trackList = songRepository.getPlaylistSongEntities(playlistId).getList().toMutableList()
            moves.forEach { (from, to) ->
                trackList.swap(from, to)
            }
            songRepository.updatePlaylistSongEntities(trackList.mapIndexed { index, entity ->
                entity.copy(idInPlaylist = index)
            })
        }
    }

    fun removeFromPlaylist(playlistId: Int, idInPlaylist: Int) {
        viewModelScope.launch {
            songRepository.removeSongFromPlaylist(playlistId, idInPlaylist)
        }
    }
}