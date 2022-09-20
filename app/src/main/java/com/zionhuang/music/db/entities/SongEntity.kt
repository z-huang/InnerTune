package com.zionhuang.music.db.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zionhuang.music.constants.MediaConstants.STATE_NOT_DOWNLOADED
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
@Entity(tableName = "song")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val duration: Int = 0, // in seconds
    val thumbnailUrl: String? = null,
    val albumId: String? = null,
    val albumName: String? = null,
    val liked: Boolean = false,
    val totalPlayTime: Long = 0, // in milliseconds
    val isTrash: Boolean = false,
    @ColumnInfo(name = "download_state")
    val downloadState: Int = STATE_NOT_DOWNLOADED,
    @ColumnInfo(name = "create_date")
    val createDate: LocalDateTime = LocalDateTime.now(),
    @ColumnInfo(name = "modify_date")
    val modifyDate: LocalDateTime = LocalDateTime.now(),
) : Parcelable
