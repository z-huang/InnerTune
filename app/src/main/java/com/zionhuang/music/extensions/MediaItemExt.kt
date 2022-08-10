package com.zionhuang.music.extensions

import android.support.v4.media.MediaDescriptionCompat
import com.google.android.exoplayer2.MediaItem
import com.zionhuang.innertube.models.SongItem
import com.zionhuang.music.constants.MediaConstants.EXTRA_MEDIA_METADATA
import com.zionhuang.music.db.entities.ArtistEntity.Companion.generateArtistId
import com.zionhuang.music.db.entities.Song
import com.zionhuang.music.models.MediaMetadata

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
    .setTag(MediaMetadata(
        id = song.id,
        title = song.title,
        artists = artists.map {
            MediaMetadata.Artist(
                id = it.id,
                name = it.name
            )
        },
        duration = song.duration,
        thumbnailUrl = song.thumbnailUrl,
        album = album?.let {
            MediaMetadata.Album(
                id = it.id,
                title = it.title,
                year = it.year
            )
        }
    ))
    .build()

fun SongItem.toMediaItem() = MediaItem.Builder()
    .setMediaId(id)
    .setUri(id)
    .setCustomCacheKey(id)
    .setTag(MediaMetadata(
        id = id,
        title = title,
        artists = artists.map {
            MediaMetadata.Artist(
                id = it.navigationEndpoint?.browseEndpoint?.browseId ?: generateArtistId(),
                name = it.text
            )
        },
        duration = duration ?: -1,
        thumbnailUrl = thumbnails.lastOrNull()?.url,
        album = album?.let {
            MediaMetadata.Album(
                id = it.navigationEndpoint.browseId,
                title = it.text,
                year = albumYear
            )
        }
    ))
    .build()

fun MediaMetadata.toMediaItem() = MediaItem.Builder()
    .setMediaId(id)
    .setUri(id)
    .setCustomCacheKey(id)
    .setTag(this)
    .build()