package com.zionhuang.music.extensions

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

fun ExoPlayer.findMediaItemById(mediaId: String): MediaItem? {
    for (i in 0 until mediaItemCount) {
        val item = getMediaItemAt(i)
        if (item.mediaId == mediaId) {
            return item
        }
    }
    return null
}