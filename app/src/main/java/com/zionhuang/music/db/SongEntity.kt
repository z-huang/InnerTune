package com.zionhuang.music.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zionhuang.music.download.DownloadTask.Companion.STATE_NOT_DOWNLOADED
import com.zionhuang.music.models.SongParcel
import java.util.*

@Entity(tableName = "song")
data class SongEntity(
        @PrimaryKey val id: String,
        var title: String? = null,
        var artist: String? = null,
        var duration: Int = 0, // in seconds
        var liked: Boolean = false,
        @ColumnInfo(name = "download_state") var downloadState: Int = STATE_NOT_DOWNLOADED,
        @ColumnInfo(name = "create_date") var createDate: Date = Date(),
        @ColumnInfo(name = "modify_date") var modifyDate: Date = Date(),
) {
    constructor(videoId: String) : this(id = videoId)

    companion object {
        @JvmStatic
        fun fromSongParcel(song: SongParcel) = SongEntity(
                id = song.id,
                title = song.title,
                artist = song.artist
        )
    }
}