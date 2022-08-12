package com.zionhuang.music.extensions

import android.support.v4.media.MediaDescriptionCompat
import com.google.android.exoplayer2.MediaItem
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.music.constants.MediaConstants.EXTRA_MEDIA_METADATA
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.models.MediaMetadata
import com.zionhuang.music.models.toMediaMetadata

val MediaItem.metadata: MediaMetadata?
    get() = localConfiguration?.tag as? MediaMetadata

fun MediaDescriptionCompat.toMediaItem() = MediaItem.Builder()
    .setMediaId(mediaId ?: error("No media id"))
    .setUri(mediaId)
    .setCustomCacheKey(mediaId)
    .setTag(extras?.getParcelable<MediaMetadata>(EXTRA_MEDIA_METADATA) ?: error("No media metadata"))
    .build()

fun Song.toMediaItem() = MediaItem.Builder()
    .setMediaId(song.id)
    .setUri(song.id)
    .setCustomCacheKey(song.id)
    .setTag(toMediaMetadata())
    .build()

fun SongItem.toMediaItem() = MediaItem.Builder()
    .setMediaId(id)
    .setUri(id)
    .setCustomCacheKey(id)
    .setTag(toMediaMetadata())
    .build()

fun MediaMetadata.toMediaItem() = MediaItem.Builder()
    .setMediaId(id)
    .setUri(id)
    .setCustomCacheKey(id)
    .setTag(this)
    .build()