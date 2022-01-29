package com.zionhuang.music.models.base

import androidx.lifecycle.LiveData
import com.zionhuang.music.models.SortInfo

interface IMutableSortInfo : ISortInfo {
    override var type: Int
    override var isDescending: Boolean
    val liveData: LiveData<SortInfo>

    fun toggleIsDescending() {
        isDescending = !isDescending
    }
}