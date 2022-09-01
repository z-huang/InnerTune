package com.zionhuang.music.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
@Entity(tableName = "album")
data class AlbumEntity(
    @PrimaryKey val id: String,
    val title: String,
    val year: Int? = null,
    val thumbnailUrl: String? = null,
    val songCount: Int,
    val duration: Int,
    val createDate: LocalDateTime = LocalDateTime.now(),
    val lastUpdateTime: LocalDateTime = LocalDateTime.now(),
) : Parcelable