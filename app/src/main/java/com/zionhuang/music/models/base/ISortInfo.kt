package com.zionhuang.music.models.base

import com.zionhuang.music.models.SortType

interface ISortInfo<T : SortType> {
    val type: T
    val isDescending: Boolean
}