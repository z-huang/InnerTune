package com.zionhuang.music.db.entities

import androidx.room.Embedded

data class Artist(
    @Embedded
    val artist: ArtistEntity,
    val songCount: Int,
) : LocalItem() {
    override val id: String
        get() = artist.id
}
