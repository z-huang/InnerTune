package com.zionhuang.music.models.sortInfo

interface ISortInfo<T : SortType> {
    val type: T
    val isDescending: Boolean
}