package com.zionhuang.music.models.base

import com.zionhuang.music.constants.SongSortType
import com.zionhuang.music.models.SortInfo

interface ISortInfo {
    @SongSortType
    val type: Int
    val isDescending: Boolean

    fun parcelize() = SortInfo(type, isDescending)
}