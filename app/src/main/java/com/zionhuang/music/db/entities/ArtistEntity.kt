package com.zionhuang.music.db.entities

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.apache.commons.lang3.RandomStringUtils
import java.time.LocalDateTime

@Immutable
@Entity(tableName = "artist")
data class ArtistEntity(
    @PrimaryKey val id: String,
    val name: String,
    val thumbnailUrl: String? = null,
    val bannerUrl: String? = null,
    val description: String? = null,
    val createDate: LocalDateTime = LocalDateTime.now(),
    val lastUpdateTime: LocalDateTime = LocalDateTime.now(),
) {
    override fun toString(): String = name

    val isYouTubeArtist: Boolean
        get() = id.startsWith("UC")

    val isLocalArtist: Boolean
        get() = id.startsWith("LA")

    companion object {
        fun generateArtistId() = "LA" + RandomStringUtils.random(8, true, false)
    }
}
