package com.zionhuang.music.db.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class PlaylistWithSongs(
        @Embedded val playlist: PlaylistEntity,
        @Relation(entity = SongEntity::class,
                parentColumn = "playlistId",
                entityColumn = "songId",
                associateBy = Junction(PlaylistSongEntity::class))
        val songs: List<Song>,
)
