package com.zionhuang.music.models

import android.support.v4.media.session.MediaSessionCompat

class MediaSessionQueueData {
    var items: List<MediaSessionCompat.QueueItem> = emptyList()
    fun update(newItems: List<MediaSessionCompat.QueueItem>): MediaSessionQueueData = apply {
        items = newItems
    }
}