package com.zionhuang.music.extensions

import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.zionhuang.music.models.MediaMetadata

fun Player.findMediaItemById(mediaId: String): MediaItem? {
    for (i in 0 until mediaItemCount) {
        val item = getMediaItemAt(i)
        if (item.mediaId == mediaId) {
            return item
        }
    }
    return null
}

fun Player.mediaItemIndexOf(mediaId: String?): Int? {
    if (mediaId == null) return null
    for (i in 0 until mediaItemCount) {
        val item = getMediaItemAt(i)
        if (item.mediaId == mediaId) {
            return i
        }
    }
    return null
}

val Player.currentMetadata: MediaMetadata?
    get() = currentMediaItem?.metadata

val Player.mediaItems: List<MediaItem>
    get() = object : AbstractList<MediaItem>() {
        override val size: Int
            get() = mediaItemCount

        override fun get(index: Int): MediaItem = getMediaItemAt(index)
    }