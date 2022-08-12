package com.zionhuang.music.db.entities

import androidx.room.Embedded

data class Album(
    @Embedded
    val album: AlbumEntity,
    val songCount: Int,
) : LocalItem() {
    override val id: String
        get() = album.id
}
