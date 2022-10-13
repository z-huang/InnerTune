package com.zionhuang.music.models

import android.content.Context
import android.os.Parcelable
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.music.db.entities.ArtistEntity
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.db.entities.SongEntity
import com.zionhuang.music.ui.bindings.resizeThumbnailUrl
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import kotlin.math.roundToInt

@Parcelize
data class MediaMetadata(
    val id: String,
    val title: String,
    val artists: List<Artist>,
    val duration: Int,
    val thumbnailUrl: String? = null,
    val album: Album? = null,
) : Parcelable, Serializable {
    @Parcelize
    data class Artist(
        val id: String,
        val name: String,
    ) : Parcelable, Serializable

    @Parcelize
    data class Album(
        val id: String,
        val title: String,
        val year: Int? = null,
    ) : Parcelable, Serializable

    fun toMediaDescription(context: Context): MediaDescriptionCompat = builder
        .setMediaId(id)
        .setTitle(title)
        .setSubtitle(artists.joinToString { it.name })
        .setDescription(artists.joinToString { it.name })
        .setIconUri(thumbnailUrl?.let { resizeThumbnailUrl(it, (512 * context.resources.displayMetrics.density).roundToInt(), null) }?.toUri())
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
            title = it.title,
            year = it.year
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
            id = it.navigationEndpoint?.browseEndpoint?.browseId ?: ArtistEntity.generateArtistId(),
            name = it.text
        )
    },
    duration = duration ?: 0,
    thumbnailUrl = thumbnails.lastOrNull()?.url,
    album = album?.let {
        MediaMetadata.Album(
            id = it.navigationEndpoint.browseId,
            title = it.text,
            year = albumYear
        )
    }
)