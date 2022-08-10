package com.zionhuang.music.constants

import androidx.annotation.IntDef

object MediaConstants {
    const val EXTRA_MEDIA_METADATA = "media_metadata"
    const val EXTRA_MEDIA_METADATA_ITEMS = "media_metadata_items"
    const val EXTRA_QUEUE_DATA = "queue_data"
    const val EXTRA_SONG_ID = "song_id"
    const val EXTRA_SONG = "song"
    const val EXTRA_SONG_ITEM = "song_item"
    const val EXTRA_SONGS = "songs"
    const val EXTRA_ARTIST_ID = "artist_id"
    const val EXTRA_ARTIST = "artist"
    const val EXTRA_ARTIST_NAMES = "artist_names"
    const val EXTRA_ARTIST_IDS = "artist_ids"
    const val EXTRA_ALBUM_ID = "album_id"
    const val EXTRA_PLAYLIST_ID = "playlist_id"
    const val EXTRA_PLAYLIST = "playlist"
    const val EXTRA_ARTWORK_TYPE = "artwork_type"
    const val EXTRA_DURATION = "duration"


    @IntDef(TYPE_SQUARE, TYPE_RECTANGLE)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ArtworkType

    const val TYPE_SQUARE = 0
    const val TYPE_RECTANGLE = 1

    const val STATE_NOT_DOWNLOADED = 0
    const val STATE_PREPARING = 1
    const val STATE_DOWNLOADING = 2
    const val STATE_DOWNLOADED = 3
}