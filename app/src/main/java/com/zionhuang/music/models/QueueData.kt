package com.zionhuang.music.models

import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat

class QueueData {
    var items: List<MediaSessionCompat.QueueItem> = emptyList()
    fun update(controller: MediaControllerCompat, list: List<MediaSessionCompat.QueueItem>): QueueData = apply {
        items = list
    }
}