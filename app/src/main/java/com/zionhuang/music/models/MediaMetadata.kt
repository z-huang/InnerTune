package com.zionhuang.music.models

import android.content.Context
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.compose.runtime.Immutable
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.music.db.entities.*
import com.zionhuang.music.ui.utils.resize
import kotlin.math.roundToInt

@Immutable
data class MediaMetadata(
    val id: String,
    val title: String,
    val artists: List<Artist>,
    val duration: Int,
    val thumbnailUrl: String? = null,
    val album: Album? = null,
) {
    data class Artist(
        val id: String?,
        val name: String,
    )

    data class Album(
        val id: String,
        val title: String,
    )

    fun toMediaDescription(context: Context): MediaDescriptionCompat = builder
        .setMediaId(id)
        .setTitle(title)
        .setSubtitle(artists.joinToString { it.name })
        .setDescription(artists.joinToString { it.name })
        .setIconUri(thumbnailUrl?.resize((512 * context.resources.displayMetrics.density).roundToInt(), null)?.toUri())
        .setExtras(bundleOf(
            METADATA_KEY_DURATION to duration * 1000L,
            METADATA_KEY_ARTIST to artists.joinToString { it.name },
            METADATA_KEY_ALBUM to album?.title
        ))
        .build()

    fun toSongEntity() = SongEntity(
        id = id,
        title = title,
        duration = duration,
        thumbnailUrl = thumbnailUrl,
        albumId = album?.id,
        albumName = album?.title
    )

    companion object {
        private val builder = MediaDescriptionCompat.Builder()
    }
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
    thumbnailUrl = thumbnail,
    album = album?.let {
        MediaMetadata.Album(
            id = it.id,
            title = it.name
        )
    }
)
