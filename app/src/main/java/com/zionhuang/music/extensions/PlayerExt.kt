package com.zionhuang.music.extensions

import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.zionhuang.music.playback.CustomMetadata

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

val Player.currentMetadata: CustomMetadata?
    get() = currentMediaItem?.metadata
