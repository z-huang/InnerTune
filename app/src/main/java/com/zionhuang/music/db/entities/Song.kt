package com.zionhuang.music.db.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Relation
import com.zionhuang.music.constants.MediaConstants.ArtworkType
import com.zionhuang.music.constants.MediaConstants.STATE_NOT_DOWNLOADED
import com.zionhuang.music.constants.MediaConstants.TYPE_RECTANGLE
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Song(
        val songId: String,
        var title: String? = null,
        var artistId: Int = 0,
        @Relation(entity = ArtistEntity::class, parentColumn = "artistId", entityColumn = "id", projection = ["name"])
        var artistName: String = "",
        var channelId: String = "",
        @Relation(entity = ChannelEntity::class, parentColumn = "channelId", entityColumn = "id", projection = ["name"])
        var channelName: String = "",
        @ArtworkType val artworkType: Int = TYPE_RECTANGLE,
        var duration: Int = 0, // in seconds
        var liked: Boolean = false,
        @ColumnInfo(name = "download_state") var downloadState: Int = STATE_NOT_DOWNLOADED,
        @ColumnInfo(name = "create_date") var createDate: Date = Date(),
        @ColumnInfo(name = "modify_date") var modifyDate: Date = Date(),
) : Parcelable