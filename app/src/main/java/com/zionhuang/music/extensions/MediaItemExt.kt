package com.zionhuang.music.extensions

import android.content.Context
import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import com.google.android.exoplayer2.MediaItem
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.download.DownloadTask.Companion.STATE_DOWNLOADED
import com.zionhuang.music.models.SongParcel
import com.zionhuang.music.playback.CustomMetadata
import com.zionhuang.music.playback.CustomMetadata.Companion.toCustomMetadata
import com.zionhuang.music.youtube.newpipe.SearchCacheData
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
        .setMediaId(id)
        .setUri(Uri.Builder()
                .scheme("music")
                .authority(id)
                .appendQueryParameter("fromLocal", if (downloadState == STATE_DOWNLOADED) "1" else "0")
                .build())
        .setTag(toCustomMetadata(context))
        .build()

fun SongParcel.toMediaItem(): MediaItem = toCustomMetadata().toMediaItem()

fun StreamInfoItem.toMediaItem(): MediaItem? = toCustomMetadata()?.toMediaItem()

fun String.toMediaItem(): MediaItem = mediaItemBuilder
        .setMediaId(this)
        .setUri("music://$this")
        .build()

fun MediaDescriptionCompat.toMediaItem() = mediaItemBuilder
        .setMediaId(mediaId)
        .setUri("music://$mediaId")
        .setTag(toCustomMetadata())
        .build()

fun List<Song>.toMediaItems(context: Context): List<MediaItem> = map { it.toMediaItem(context) }

fun SearchCacheData.toMediaItems(): List<MediaItem> = items.filterIsInstance<StreamInfoItem>().mapNotNull { it.toMediaItem() }
