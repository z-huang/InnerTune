package com.zionhuang.music.db.entities

import androidx.room.*
import com.zionhuang.music.download.DownloadTask.Companion.STATE_NOT_DOWNLOADED
import java.util.*

@Entity(tableName = "song",
        indices = [
            Index(value = ["id"], unique = true),
            Index(value = ["artistId"]),
            Index(value = ["channelId"])],
        foreignKeys = [
            ForeignKey(
                    entity = ArtistEntity::class,
                    parentColumns = ["id"],
                    childColumns = ["artistId"]),
            ForeignKey(
                    entity = ChannelEntity::class,
                    parentColumns = ["id"],
                    childColumns = ["channelId"])])
data class SongEntity(
        @PrimaryKey val id: String,
        var title: String? = null,
        var artistId: Int = 0,
        var channelId: String? = null,
        var duration: Int = 0, // in seconds
        var liked: Boolean = false,
        @ColumnInfo(name = "download_state") var downloadState: Int = STATE_NOT_DOWNLOADED,
        @ColumnInfo(name = "create_date") var createDate: Date = Date(),
        @ColumnInfo(name = "modify_date") var modifyDate: Date = Date(),
) {
    constructor(videoId: String) : this(id = videoId)
}