package com.zionhuang.music.db.entities

import com.zionhuang.music.constants.Constants.ARTIST_HEADER_ID
import com.zionhuang.music.constants.Constants.SONG_HEADER_ID
import com.zionhuang.music.models.ArtistSortType
import com.zionhuang.music.models.SongSortType
import com.zionhuang.music.models.SortInfo

sealed class LocalBaseItem {
    abstract val id: String
}

sealed class LocalItem : LocalBaseItem()

data class SongHeader(
    val songCount: Int,
    val sortInfo: SortInfo<SongSortType>,
) : LocalBaseItem() {
    override val id = SONG_HEADER_ID
}

data class ArtistHeader(
    val artistCount: Int,
    val sortInfo: SortInfo<ArtistSortType>,
) : LocalBaseItem() {
    override val id = ARTIST_HEADER_ID
}