package com.zionhuang.music.db.entities

import androidx.room.Junction
import androidx.room.Relation

data class PlaylistSong(
        val playlistId: Int,
        @Relation(entity = SongEntity::class,
                parentColumn = "playlistId",
                entityColumn = "songId",
                associateBy = Junction(PlaylistSongEntity::class))
        val song: Song,
)
