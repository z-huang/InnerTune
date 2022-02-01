package com.zionhuang.music.constants

import com.zionhuang.music.db.entities.Song

object Constants {
    const val EMPTY_SONG_ID = "empty_song"

    const val HEADER_ITEM_ID = "\$HEADER$"
    val HEADER_PLACEHOLDER_SONG = Song(id = HEADER_ITEM_ID, title = "")
    const val TYPE_HEADER = 0
    const val TYPE_ITEM = 1

    const val APP_URL = "https://github.com/z-huang/music"
    const val NEWPIPE_EXTRACTOR_URL = "https://github.com/TeamNewPipe/NewPipeExtractor"
}