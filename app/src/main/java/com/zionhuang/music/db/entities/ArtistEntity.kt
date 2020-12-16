package com.zionhuang.music.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "artist",
        indices = [Index(value = ["id"], unique = true)])
data class ArtistEntity(
        @PrimaryKey(autoGenerate = true) val id: Int? = null,
        val name: String,
) {
    override fun toString(): String = name
}
