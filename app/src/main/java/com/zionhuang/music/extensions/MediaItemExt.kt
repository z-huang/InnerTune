package com.zionhuang.music.extensions

import android.content.Context
import android.support.v4.media.MediaDescriptionCompat
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.MediaMetadata
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.innertube.models.VideoItem
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.models.MediaData
import com.zionhuang.music.models.toMediaData
import org.schabi.newpipe.extractor.stream.StreamInfoItem

val MediaItem.metadata: MediaData?
    get() = localConfiguration?.tag as? MediaData

private val mediaItemBuilder = MediaItem.Builder()

fun MediaData.toMediaItem() = mediaItemBuilder
    .setMediaId(id)
    .setUri(id)
    .setCustomCacheKey(id)
    .setTag(this)
    .build()

fun Song.toMediaItem(context: Context) = toMediaData(context).toMediaItem()

fun StreamInfoItem.toMediaItem() = toMediaData().toMediaItem()

fun MediaDescriptionCompat.toMediaItem() = toMediaData().toMediaItem()

fun List<Song>.toMediaItems(context: Context): List<MediaItem> = map { it.toMediaItem(context) }

fun SongItem.toMediaItem() = MediaItem.Builder()
    .setMediaId(id)
    .setUri(id)
    .setCustomCacheKey(id)
    .setTag(MediaData(
        id,
        title,
        artists.joinToString { it.text },
        null,
        thumbnails.lastOrNull()?.url
    ))
    .setMediaMetadata(MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artists.joinToString { it.text })
        .setAlbumTitle(album?.text)
        .setArtworkUri(thumbnails.lastOrNull()?.url?.toUri())
        .build())
    .build()

fun VideoItem.toMediaItem() = MediaItem.Builder()
    .setMediaId(id)
    .setUri(id)
    .setCustomCacheKey(id)
    .setTag(MediaData(
        id,
        title,
        artist.text,
        null,
        thumbnails.lastOrNull()?.url
    ))
    .setMediaMetadata(MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist.text)
        .setArtworkUri(thumbnails.lastOrNull()?.url?.toUri())
        .build())
    .build()
