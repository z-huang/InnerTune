package com.zionhuang.music.models

import android.os.Parcelable
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import com.zionhuang.music.constants.MediaConstants.EXTRA_MEDIA_METADATA
import com.zionhuang.music.db.entities.SongEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaMetadata(
    val id: String,
    val title: String,
    val artists: List<Artist>,
    val duration: Int,
    val thumbnailUrl: String? = null,
    val album: Album? = null,
) : Parcelable {
    @Parcelize
    data class Artist(
        val id: String,
        val name: String,
    ) : Parcelable

    @Parcelize
    data class Album(
        val id: String,
        val title: String,
        val year: Int?,
    ) : Parcelable

    fun toMediaDescription(): MediaDescriptionCompat = builder
        .setMediaId(id)
        .setTitle(title)
        .setSubtitle(artists.joinToString { it.name })
        .setDescription(artists.joinToString { it.name })
        .setIconUri(thumbnailUrl?.toUri())
        .setExtras(bundleOf(
            METADATA_KEY_ARTIST to artists.joinToString { it.name },
            EXTRA_MEDIA_METADATA to this
        ))
        .build()

    fun toSongEntity() = SongEntity(
        id = id,
        title = title,
        duration = duration,
        thumbnailUrl = thumbnailUrl
    )

    companion object {
        private val builder = MediaDescriptionCompat.Builder()
    }
}
