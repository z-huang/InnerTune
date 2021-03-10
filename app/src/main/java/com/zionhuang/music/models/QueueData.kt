package com.zionhuang.music.models

import android.support.v4.media.session.MediaSessionCompat

class QueueData {
    var items: List<MediaSessionCompat.QueueItem> = emptyList()
    fun update(newItems: List<MediaSessionCompat.QueueItem>): QueueData = apply {
        items = newItems
    }
}