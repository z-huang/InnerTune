package com.zionhuang.music.db.entities

import com.zionhuang.music.constants.Constants.SONG_HEADER_ID
import com.zionhuang.music.models.base.IMutableSortInfo

sealed class LocalBaseItem {
    abstract val id: String
}

sealed class LocalItem : LocalBaseItem()

class SongHeader(
    val songCount: Int,
    val sortInfo: IMutableSortInfo,
) : LocalBaseItem() {
    override val id = SONG_HEADER_ID
}