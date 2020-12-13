package com.zionhuang.music.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "channel",
        indices = [Index(value = ["id"], unique = true)])
data class ChannelEntity(
        @PrimaryKey val id: String,
        val name: String
)
