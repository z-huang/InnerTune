package com.zionhuang.music.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "stream",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StreamEntity(
    @PrimaryKey val songId: String,
    val mimeType: String,
    val bitrate: Int?,
    val contentLength: Long? = null,
    val loudnessDb: Float? = null,
)
