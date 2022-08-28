package com.zionhuang.music.models.sortInfo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

abstract class SortInfoPreference<T : SortType> : ISortInfo<T> {
    abstract override var type: T
    abstract override var isDescending: Boolean
    protected abstract val typeFlow: Flow<T>
    protected abstract val isDescendingFlow: Flow<Boolean>

    fun toggleIsDescending() {
        isDescending = !isDescending
    }

    val currentInfo: SortInfo<T>
        get() = SortInfo(type, isDescending)

    val flow: Flow<SortInfo<T>>
        get() = typeFlow.combine(isDescendingFlow) { type, isDescending ->
            SortInfo(type, isDescending)
        }
}