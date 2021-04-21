package com.zionhuang.music.playback

import android.content.Context
import android.support.v4.media.MediaDescriptionCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import com.zionhuang.music.constants.MediaConstants.ArtworkType
import com.zionhuang.music.constants.MediaConstants.EXTRA_ARTWORK_TYPE
import com.zionhuang.music.constants.MediaConstants.TYPE_RECTANGLE
import com.zionhuang.music.constants.MediaConstants.TYPE_SQUARE
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.extensions.getArtworkFile
import com.zionhuang.music.playback.CustomMetadata.Companion.EMPTY_MEDIA_DESCRIPTION
import com.zionhuang.music.youtube.extractors.YouTubeStreamExtractor
import org.schabi.newpipe.extractor.stream.StreamInfoItem

data class CustomMetadata(
        val id: String,
        val title: String?,
        val artist: String?,
        var artwork: String?,
        @ArtworkType val artworkType: Int,
) {
    fun toMediaDescription(): MediaDescriptionCompat = builder
            .setMediaId(id)
            .setTitle(title)
            .setSubtitle(artist)
            .setDescription(artist)
            .setIconUri(artwork?.toUri())
            .setExtras(bundleOf(EXTRA_ARTWORK_TYPE to artworkType.toLong()))
            .build()

    companion object {
        private val builder = MediaDescriptionCompat.Builder()

        val EMPTY_MEDIA_DESCRIPTION: MediaDescriptionCompat = builder.build()

        fun Song.toCustomMetadata(context: Context) = CustomMetadata(songId, title, artistName, context.getArtworkFile(songId).canonicalPath, artworkType)

        fun StreamInfoItem.toCustomMetadata(): CustomMetadata? = YouTubeStreamExtractor.extractId(url)?.let { CustomMetadata(it, name, uploaderName, thumbnailUrl, if ("music.youtube.com" in url) TYPE_SQUARE else TYPE_RECTANGLE) }

        fun MediaDescriptionCompat.toCustomMetadata(): CustomMetadata = CustomMetadata(mediaId!!, title.toString(), subtitle.toString(), iconUri.toString(), extras!!.getInt(EXTRA_ARTWORK_TYPE))
    }
}

fun CustomMetadata?.toMediaDescription(): MediaDescriptionCompat = this?.toMediaDescription()
        ?: EMPTY_MEDIA_DESCRIPTION