package com.zionhuang.music.playback

import android.content.Context
import android.support.v4.media.MediaDescriptionCompat
import androidx.core.net.toUri
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.getArtworkFile
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.playback.CustomMetadata.Companion.EMPTY_MEDIA_DESCRIPTION
import com.zionhuang.music.youtube.extractors.YouTubeStreamExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItem

data class CustomMetadata(
        val id: String,
        val title: String?,
        val artist: String?,
        val artwork: String?,
) {
    fun toMediaDescription(): MediaDescriptionCompat = builder
            .setMediaId(id)
            .setTitle(title)
            .setSubtitle(artist)
            .setDescription(artist)
            .setIconUri(artwork?.toUri())
            .build()

    companion object {
        private val builder = MediaDescriptionCompat.Builder()

        val EMPTY_MEDIA_DESCRIPTION: MediaDescriptionCompat = builder.build()

        fun Song.toCustomMetadata(context: Context) = CustomMetadata(id, title, artistName, context.getArtworkFile(id).canonicalPath)

        fun SongParcel.toCustomMetadata() = CustomMetadata(id, title, artist, artworkUrl)

        fun StreamInfoItem.toCustomMetadata(): CustomMetadata? = YouTubeStreamExtractor.extractId(url)?.let { CustomMetadata(it, name, uploaderName, thumbnailUrl) }
    }
}

private val builder = MediaDescriptionCompat.Builder()

fun CustomMetadata?.toMediaDescription(): MediaDescriptionCompat = this?.toMediaDescription()
        ?: EMPTY_MEDIA_DESCRIPTION