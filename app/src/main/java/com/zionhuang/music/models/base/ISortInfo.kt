package com.zionhuang.music.models.base

import com.zionhuang.music.models.SortInfo

interface ISortInfo {
    val type: Int
    val isDescending: Boolean

    fun parcelize() = SortInfo(type, isDescending)
}