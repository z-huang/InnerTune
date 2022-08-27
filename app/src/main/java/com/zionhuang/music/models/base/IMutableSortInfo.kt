package com.zionhuang.music.models.base

import com.zionhuang.music.models.SortInfo
import com.zionhuang.music.models.SortType

interface IMutableSortInfo<T : SortType> : ISortInfo<T> {
    override var type: T
    override var isDescending: Boolean

    fun toggleIsDescending() {
        isDescending = !isDescending
    }

    val currentInfo: SortInfo<T>
        get() = SortInfo(type, isDescending)
}