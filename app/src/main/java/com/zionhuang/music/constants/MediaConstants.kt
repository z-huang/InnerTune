package com.zionhuang.music.constants

object MediaConstants {
    const val EXTRA_MEDIA_METADATA = "media_metadata"
    const val EXTRA_MEDIA_METADATA_ITEMS = "media_metadata_items"
    const val EXTRA_SONG = "song"
    const val EXTRA_ARTIST = "artist"
    const val EXTRA_PLAYLIST = "playlist"
    const val EXTRA_BLOCK = "block"

    const val STATE_NOT_DOWNLOADED = 0
    const val STATE_PREPARING = 1
    const val STATE_DOWNLOADING = 2
    const val STATE_DOWNLOADED = 3
}