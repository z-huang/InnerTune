package com.zionhuang.music.extensions

import android.content.Context
import com.google.android.exoplayer2.MediaItem
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.playback.CustomMetadata
import com.zionhuang.music.playback.CustomMetadata.Companion.toCustomMetadata
import com.zionhuang.music.youtube.newpipe.SearchCacheData
import org.schabi.newpipe.extractor.stream.StreamInfoItem

private val mediaItemBuilder = MediaItem.Builder()

fun CustomMetadata.toMediaItem() = mediaItemBuilder
        .setMediaId(id)
        .setUri("music://$id")
        .setTag(this)
        .build()

fun Song.toMediaItem(context: Context): MediaItem = toCustomMetadata(context).toMediaItem()

fun SongParcel.toMediaItem(): MediaItem = toCustomMetadata().toMediaItem()

fun StreamInfoItem.toMediaItem(): MediaItem? = toCustomMetadata()?.toMediaItem()

fun String.toMediaItem(): MediaItem = mediaItemBuilder
        .setMediaId(this)
        .setUri("music://$this")
        .build()

fun List<Song>.toMediaItems(context: Context): List<MediaItem> = map { it.toMediaItem(context) }

fun SearchCacheData.toMediaItems(): List<MediaItem> = items.filterIsInstance<StreamInfoItem>().mapNotNull { it.toMediaItem() }
