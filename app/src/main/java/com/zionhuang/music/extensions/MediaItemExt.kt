package com.zionhuang.music.extensions

import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import com.google.android.exoplayer2.MediaItem
import com.zionhuang.music.constants.Constants.FROM_LOCAL
import com.zionhuang.music.constants.MediaConstants.STATE_DOWNLOADED
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.models.MediaData
import com.zionhuang.music.models.MediaData.Companion.toMediaData
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem

val MediaItem.metadata: MediaData?
    get() = playbackProperties?.tag as? MediaData

private val mediaItemBuilder = MediaItem.Builder()

fun MediaData.toMediaItem() = mediaItemBuilder
    .setMediaId(id)
    .setUri("music://$id")
    .setTag(this)
    .build()

fun Song.toMediaItem(context: Context): MediaItem = mediaItemBuilder
    .setMediaId(songId)
    .setUri(
        Uri.Builder()
            .scheme("music")
            .authority(songId)
            .appendQueryParameter(FROM_LOCAL, if (downloadState == STATE_DOWNLOADED) "1" else "0")
            .build()
    )
    .setTag(toMediaData(context))
    .build()

fun StreamInfoItem.toMediaItem(): MediaItem? = toMediaData()?.toMediaItem()

fun MediaDescriptionCompat.toMediaItem() = mediaItemBuilder
    .setMediaId(mediaId!!)
    .setUri("music://$mediaId")
    .setTag(toMediaData())
    .build()

fun List<Song>.toMediaItems(context: Context): List<MediaItem> = map { it.toMediaItem(context) }

fun List<InfoItem>.toMediaItems(): List<MediaItem> =
    filterIsInstance<StreamInfoItem>().mapNotNull { it.toMediaItem() }
