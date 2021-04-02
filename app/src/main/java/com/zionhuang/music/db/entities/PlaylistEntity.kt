package com.zionhuang.music.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "playlist",
        indices = [Index(value = ["id"], unique = true)])
data class PlaylistEntity(
        @PrimaryKey(autoGenerate = true) val id: Int? = null,
        val name: String,
)
