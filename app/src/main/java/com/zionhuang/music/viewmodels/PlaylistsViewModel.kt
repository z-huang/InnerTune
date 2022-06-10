package com.zionhuang.music.viewmodels

import android.app.Application
import android.content.Context
import androidx.core.os.bundleOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zionhuang.music.constants.MediaConstants.EXTRA_PLAYLIST
import com.zionhuang.music.db.entities.PlaylistEntity
import com.zionhuang.music.extensions.show
import com.zionhuang.music.repos.SongRepository
import com.zionhuang.music.ui.fragments.dialogs.EditPlaylistDialog
import com.zionhuang.music.ui.listeners.PlaylistPopupMenuListener
import kotlinx.coroutines.launch

class PlaylistsViewModel(application: Application) : AndroidViewModel(application) {
    private val songRepository = SongRepository

    val popupMenuListener = object : PlaylistPopupMenuListener {
        override fun editPlaylist(playlist: PlaylistEntity, context: Context) {
            EditPlaylistDialog().apply {
                arguments = bundleOf(EXTRA_PLAYLIST to playlist)
            }.show(context)
        }

        override fun deletePlaylist(playlist: PlaylistEntity) {
            viewModelScope.launch {
                songRepository.deletePlaylist(playlist)
            }
        }
    }
}