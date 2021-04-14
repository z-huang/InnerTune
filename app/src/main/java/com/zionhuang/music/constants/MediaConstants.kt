package com.zionhuang.music.constants

import androidx.annotation.IntDef

object MediaConstants {
    const val EXTRA_QUEUE_TYPE = "queue_type"
    const val EXTRA_QUEUE_ORDER = "queue_order"
    const val EXTRA_QUEUE_DESC = "queue_desc"
    const val EXTRA_SONG_ID = "song_id"
    const val EXTRA_SONG = "song"
    const val EXTRA_ARTIST_ID = "artist_id"
    const val EXTRA_ARTIST = "artist"
    const val EXTRA_PLAYLIST_ID = "playlist_id"
    const val EXTRA_LINK_HANDLER = "link_handler"
    const val EXTRA_QUERY_STRING = "query_string"

    @IntDef(QUEUE_NONE, QUEUE_ALL_SONG, QUEUE_PLAYLIST, QUEUE_ARTIST, QUEUE_CHANNEL, QUEUE_SINGLE, QUEUE_SEARCH, QUEUE_YT_PLAYLIST)
    @Retention(AnnotationRetention.SOURCE)
    annotation class QueueType

    const val QUEUE_NONE = 0
    const val QUEUE_ALL_SONG = 1
    const val QUEUE_PLAYLIST = 2
    const val QUEUE_ARTIST = 3
    const val QUEUE_CHANNEL = 4
    const val QUEUE_SINGLE = 5
    const val QUEUE_SEARCH = 6
    const val QUEUE_YT_PLAYLIST = 7
}