package com.zionhuang.music.db.entities

import androidx.room.*
import com.zionhuang.music.constants.MediaConstants.ArtworkType
import com.zionhuang.music.constants.MediaConstants.STATE_NOT_DOWNLOADED
import java.util.*

@Entity(
    tableName = "song",
    indices = [
        Index(value = ["id"], unique = true),
        Index(value = ["artistId"])],
    foreignKeys = [
        ForeignKey(
            entity = ArtistEntity::class,
            parentColumns = ["id"],
            childColumns = ["artistId"],
            onDelete = ForeignKey.CASCADE
        )]
)
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artistId: Int = 0,
    val duration: Int = 0, // in seconds
    val liked: Boolean = false,
    @ArtworkType val artworkType: Int,
    @ColumnInfo(name = "download_state") val downloadState: Int = STATE_NOT_DOWNLOADED,
    @ColumnInfo(name = "create_date") val createDate: Date = Date(),
    @ColumnInfo(name = "modify_date") val modifyDate: Date = Date(),
)
