package com.zionhuang.music.models

import com.zionhuang.music.models.base.ISortInfo

data class SortInfo<T : SortType>(
    override val type: T,
    override val isDescending: Boolean,
) : ISortInfo<T>

interface SortType

enum class SongSortType : SortType {
    CREATE_DATE, NAME, ARTIST
}

enum class ArtistSortType : SortType {
    CREATE_DATE, NAME, SONG_COUNT
}