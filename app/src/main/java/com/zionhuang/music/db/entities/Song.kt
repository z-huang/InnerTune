package com.zionhuang.music.db.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import androidx.room.Relation
import com.zionhuang.music.constants.MediaConstants.ArtworkType
import com.zionhuang.music.constants.MediaConstants.STATE_NOT_DOWNLOADED
import com.zionhuang.music.constants.MediaConstants.TYPE_RECTANGLE
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Song @JvmOverloads constructor(
    val id: String,
    val title: String,
    private val artistId: Int = -1,
    @Relation(entity = ArtistEntity::class, parentColumn = "artistId", entityColumn = "id", projection = ["name"])
    val artistName: String = "",
    @ArtworkType val artworkType: Int = TYPE_RECTANGLE,
    val duration: Int = -1, // in seconds
    val liked: Boolean = false,
    @ColumnInfo(name = "download_state") val downloadState: Int = STATE_NOT_DOWNLOADED,
    @ColumnInfo(name = "create_date") val createDate: Date = Date(),
    @ColumnInfo(name = "modify_date") val modifyDate: Date = Date(),
    val idInPlaylist: Int? = -1,
) : Parcelable