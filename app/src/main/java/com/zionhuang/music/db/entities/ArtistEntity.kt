package com.zionhuang.music.db.entities

import androidx.room.PrimaryKey

data class ArtistEntity(
        @PrimaryKey(autoGenerate = true) val id: Int? = null,
        val name: String,
)
