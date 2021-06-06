package com.zionhuang.music.ui.listeners

import android.content.Context
import com.zionhuang.music.db.entities.PlaylistEntity

interface PlaylistPopupMenuListener {
    fun editPlaylist(playlist: PlaylistEntity, context: Context)
    fun deletePlaylist(playlist: PlaylistEntity)
}