package com.zionhuang.music.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKey(
        @PrimaryKey(autoGenerate = true) val queryId: Long = 0,
        val query: String,
        val nextPageToken: String?
)