package com.zionhuang.music.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    tableName = "playlist",
    indices = [Index(value = ["playlistId"], unique = true)]
)
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val playlistId: Int = 0,
    val name: String,
) : Parcelable
