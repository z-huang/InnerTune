package com.zionhuang.music.constants

import androidx.annotation.IntDef

object MediaConstants {
    const val QUEUE_TYPE = "queue_type"
    const val QUEUE_ORDER = "queue_order"
    const val QUEUE_DESC = "queue_desc"
    const val SONG_ID = "song_id"
    const val SONG = "song"

    @IntDef(QUEUE_NONE, QUEUE_ALL_SONG, QUEUE_PLAYLIST, QUEUE_ARTIST, QUEUE_CHANNEL, QUEUE_SINGLE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class QueueType

    const val QUEUE_NONE = 0
    const val QUEUE_ALL_SONG = 1
    const val QUEUE_PLAYLIST = 2
    const val QUEUE_ARTIST = 3
    const val QUEUE_CHANNEL = 4
    const val QUEUE_SINGLE = 5
}