package com.zionhuang.music.models

import androidx.compose.runtime.Immutable
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.ui.utils.resize
import java.io.Serializable

@Immutable
data class MediaMetadata(
    val id: String,
    val title: String,
    val artists: List<Artist>,
    val duration: Int,
    val thumbnailUrl: String? = null,
    val album: Album? = null,
    val explicit: Boolean = false,
    // Queue Groups: identifies the queue-group instance this item belongs to.
    // null means this is a standalone queue item, not part of any group.
    val queueGroupId: String? = null,
    val queueGroupTitle: String? = null,
    val queueGroupIndex: Int? = null,
    val queueGroupSize: Int? = null,
) : Serializable {
    companion object {
        // Pinned explicitly so that future additions of nullable fields (as done here for
        // Queue Groups) don't silently change the default computed serialVersionUID and
        // break deserialization of previously-persisted queues (see PersistQueue / MusicService
        // saveQueueToDisk/restore). No serialVersionUID existed on this class before this
        // change, so this does not, by itself, restore compatibility with files persisted
        // by app versions prior to this one -- see the Stage 1 report for details.
        private const val serialVersionUID: Long = 1L
    }

    data class Artist(
        val id: String?,
        val name: String,
    ) : Serializable

    data class Album(
        val id: String,
        val title: String,
    ) : Serializable

    fun toSongEntity() = SongEntity(
        id = id,
        title = title,
        duration = duration,
        thumbnailUrl = thumbnailUrl,
        albumId = album?.id,
        albumName = album?.title
    )
}

fun Song.toMediaMetadata() = MediaMetadata(
    id = song.id,
    title = song.title,
    artists = artists.map {
        MediaMetadata.Artist(
            id = it.id,
            name = it.name
        )
    },
    duration = song.duration,
    thumbnailUrl = song.thumbnailUrl,
    album = album?.let {
        MediaMetadata.Album(
            id = it.id,
            title = it.title
        )
    } ?: song.albumId?.let { albumId ->
        MediaMetadata.Album(
            id = albumId,
            title = song.albumName.orEmpty()
        )
    }
)

fun SongItem.toMediaMetadata() = MediaMetadata(
    id = id,
    title = title,
    artists = artists.map {
        MediaMetadata.Artist(
            id = it.id,
            name = it.name
        )
    },
    duration = duration ?: -1,
    thumbnailUrl = thumbnail.resize(544, 544),
    album = album?.let {
        MediaMetadata.Album(
            id = it.id,
            title = it.name
        )
    },
    explicit = explicit
)
