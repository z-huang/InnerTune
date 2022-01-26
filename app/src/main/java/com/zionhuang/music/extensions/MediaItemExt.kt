package com.zionhuang.music.extensions

import android.content.Context
import android.support.v4.media.MediaDescriptionCompat
import com.google.android.exoplayer2.MediaItem
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.models.MediaData
import com.zionhuang.music.models.toMediaData
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem

val MediaItem.metadata: MediaData?
    get() = localConfiguration?.tag as? MediaData

private val mediaItemBuilder = MediaItem.Builder()

fun MediaData.toMediaItem() = mediaItemBuilder
    .setMediaId(id)
    .setUri("music://$id")
    .setTag(this)
    .build()

fun Song.toMediaItem(context: Context) = toMediaData(context).toMediaItem()

fun StreamInfo.toMediaItem() = toMediaData().toMediaItem()

fun StreamInfoItem.toMediaItem() = toMediaData().toMediaItem()

fun MediaDescriptionCompat.toMediaItem() = toMediaData().toMediaItem()

fun List<Song>.toMediaItems(context: Context): List<MediaItem> = map { it.toMediaItem(context) }

fun List<InfoItem>.toMediaItems(): List<MediaItem> = filterIsInstance<StreamInfoItem>().map { it.toMediaItem() }
