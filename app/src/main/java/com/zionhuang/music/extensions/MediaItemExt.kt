package com.zionhuang.music.extensions

import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import com.google.android.exoplayer2.MediaItem
import com.zionhuang.music.constants.Constants.FROM_LOCAL
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.download.DownloadTask.Companion.STATE_DOWNLOADED
import com.zionhuang.music.playback.CustomMetadata
import com.zionhuang.music.playback.CustomMetadata.Companion.toCustomMetadata
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.stream.StreamInfoItem

val MediaItem.metadata: CustomMetadata?
    get() = playbackProperties?.tag as? CustomMetadata

private val mediaItemBuilder = MediaItem.Builder()

fun CustomMetadata.toMediaItem() = mediaItemBuilder
        .setMediaId(id)
        .setUri("music://$id")
        .setTag(this)
        .build()

fun Song.toMediaItem(context: Context): MediaItem = mediaItemBuilder
        .setMediaId(songId)
        .setUri(Uri.Builder()
                .scheme("music")
                .authority(songId)
                .appendQueryParameter(FROM_LOCAL, if (downloadState == STATE_DOWNLOADED) "1" else "0")
                .build())
        .setTag(toCustomMetadata(context))
        .build()

fun StreamInfoItem.toMediaItem(): MediaItem? = toCustomMetadata()?.toMediaItem()

fun MediaDescriptionCompat.toMediaItem() = mediaItemBuilder
        .setMediaId(mediaId)
        .setUri("music://$mediaId")
        .setTag(toCustomMetadata())
        .build()

fun List<Song>.toMediaItems(context: Context): List<MediaItem> = map { it.toMediaItem(context) }

fun List<InfoItem>.toMediaItems(): List<MediaItem> = filterIsInstance<StreamInfoItem>().mapNotNull { it.toMediaItem() }
