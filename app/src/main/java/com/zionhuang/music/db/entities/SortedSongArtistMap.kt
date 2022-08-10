package com.zionhuang.music.db.entities

import androidx.room.ColumnInfo
import androidx.room.DatabaseView

@DatabaseView("SELECT * FROM song_artist_map ORDER BY position")
data class SortedSongArtistMap(
    @ColumnInfo(index = true) val songId: String,
    @ColumnInfo(index = true) val artistId: String,
    val position: Int,
)
