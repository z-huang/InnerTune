package com.zionhuang.music.models

import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat

class QueueData {
    private var queueTitle = ""
    private var queueItemList: List<MediaSessionCompat.QueueItem> = emptyList()
    private val currentIndex: Long = -1
    fun update(controller: MediaControllerCompat, list: List<MediaSessionCompat.QueueItem>): QueueData {
        //queueTitle = controller.queueTitle?.toString()
        queueItemList = list
        return this
    }
}