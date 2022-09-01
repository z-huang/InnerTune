package com.zionhuang.music.db.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import org.apache.commons.lang3.RandomStringUtils
import java.time.LocalDateTime

@Parcelize
@Entity(tableName = "playlist")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val author: String? = null,
    val authorId: String? = null,
    val year: Int? = null,
    val thumbnailUrl: String? = null,
    val createDate: LocalDateTime = LocalDateTime.now(),
    val lastUpdateTime: LocalDateTime = LocalDateTime.now(),
) : Parcelable {
    val isLocalPlaylist: Boolean
        get() = id.startsWith("LP")

    val isYouTubePlaylist: Boolean
        get() = !isLocalPlaylist

    companion object {
        fun generatePlaylistId() = "LP" + RandomStringUtils.random(8, true, false)
    }
}
