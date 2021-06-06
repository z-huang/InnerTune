package com.zionhuang.music.models

import android.content.Context
import android.os.Parcelable
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import com.zionhuang.music.constants.Constants.EMPTY_SONG_ID
import com.zionhuang.music.constants.MediaConstants
import com.zionhuang.music.constants.MediaConstants.ArtworkType
import com.zionhuang.music.constants.MediaConstants.EXTRA_ARTWORK_TYPE
import com.zionhuang.music.constants.MediaConstants.TYPE_SQUARE
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.getArtworkFile
import com.zionhuang.music.models.MediaData.Companion.EMPTY_MEDIA_DESCRIPTION
import com.zionhuang.music.youtube.extractors.YouTubeStreamExtractor
import kotlinx.parcelize.Parcelize
import org.schabi.newpipe.extractor.stream.StreamInfoItem

@Parcelize
data class MediaData(
    var id: String = EMPTY_SONG_ID,
    var title: String? = null,
    var artist: String? = null,
    var duration: Int? = null,
    var artwork: String? = null,
    @ArtworkType var artworkType: Int = TYPE_SQUARE,
) : Parcelable {
    fun pullMediaMetadata(mediaMetadata: MediaMetadataCompat): MediaData = apply {
        id = mediaMetadata.getString(METADATA_KEY_MEDIA_ID)
        title = mediaMetadata.getString(METADATA_KEY_TITLE)
        artist = mediaMetadata.getString(METADATA_KEY_DISPLAY_SUBTITLE)
        artwork = mediaMetadata.getString(METADATA_KEY_DISPLAY_ICON_URI)
        duration = (mediaMetadata.getLong(METADATA_KEY_DURATION) / 1000).toInt()
        artworkType = mediaMetadata.getLong(EXTRA_ARTWORK_TYPE).toInt()
    }

    fun toMediaDescription(): MediaDescriptionCompat = builder
        .setMediaId(id)
        .setTitle(title)
        .setSubtitle(artist)
        .setDescription(artist)
        .setIconUri(artwork?.toUri())
        .setExtras(
            bundleOf(
                EXTRA_ARTWORK_TYPE to artworkType.toLong(),
                MediaConstants.EXTRA_DURATION to duration
            )
        )
        .build()

    companion object {
        private val builder = MediaDescriptionCompat.Builder()

        val EMPTY_MEDIA_DESCRIPTION: MediaDescriptionCompat = builder.build()

        fun Song.toMediaData(context: Context) = MediaData(songId, title, artistName, duration, context.getArtworkFile(songId).canonicalPath, artworkType)

        fun StreamInfoItem.toMediaData() =
            YouTubeStreamExtractor.extractId(url)
                ?.let { MediaData(it, name, uploaderName, duration.toInt(), thumbnailUrl, if ("music.youtube.com" in url) TYPE_SQUARE else MediaConstants.TYPE_RECTANGLE) }

        fun MediaDescriptionCompat.toMediaData() =
            MediaData(mediaId!!, title.toString(), subtitle.toString(), extras?.getInt(MediaConstants.EXTRA_DURATION), iconUri.toString(), extras!!.getInt(EXTRA_ARTWORK_TYPE))
    }
}

fun MediaMetadataCompat.toMediaData(): MediaData = MediaData().pullMediaMetadata(this)

fun MediaData?.toMediaDescription(): MediaDescriptionCompat = this?.toMediaDescription()
    ?: EMPTY_MEDIA_DESCRIPTION