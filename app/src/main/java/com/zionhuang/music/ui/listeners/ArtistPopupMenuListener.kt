package com.zionhuang.music.ui.listeners

import android.content.Context
import com.zionhuang.music.db.entities.ArtistEntity

interface ArtistPopupMenuListener {
    fun editArtist(artist: ArtistEntity, context: Context)
    fun deleteArtist(artist: ArtistEntity)
}