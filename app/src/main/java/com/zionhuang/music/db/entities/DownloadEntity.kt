package com.zionhuang.music.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "download")
data class DownloadEntity(
    @PrimaryKey val id: Long,
    val songId: String,
)
