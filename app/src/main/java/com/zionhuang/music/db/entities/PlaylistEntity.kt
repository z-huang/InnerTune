package com.zionhuang.music.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "playlist",
        indices = [Index(value = ["playlistId"], unique = true)])
data class PlaylistEntity(
        @PrimaryKey(autoGenerate = true) val playlistId: Int = 0,
        val name: String,
)
