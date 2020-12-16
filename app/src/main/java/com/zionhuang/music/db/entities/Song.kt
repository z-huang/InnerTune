package com.zionhuang.music.db.entities

import androidx.room.ColumnInfo
import androidx.room.Relation
import com.zionhuang.music.download.DownloadTask.Companion.STATE_NOT_DOWNLOADED
import java.util.*

data class Song(
        val id: String,
        var title: String? = null,
        var artistId: Int = 0,
        @Relation(entity = ArtistEntity::class, parentColumn = "artistId", entityColumn = "id", projection = ["name"])
        var artistName: String = "",
        var channelId: String? = null,
        @Relation(entity = ChannelEntity::class, parentColumn = "channelId", entityColumn = "id", projection = ["name"])
        val channelName: String = "",
        var duration: Int = 0, // in seconds
        var liked: Boolean = false,
        @ColumnInfo(name = "download_state") var downloadState: Int = STATE_NOT_DOWNLOADED,
        @ColumnInfo(name = "create_date") var createDate: Date = Date(),
        @ColumnInfo(name = "modify_date") var modifyDate: Date = Date()
)