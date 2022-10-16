package com.zionhuang.music.models

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow


open class DataWrapper<T>(
    val getValue: () -> T = { throw UnsupportedOperationException() },
    val getValueAsync: suspend () -> T = { throw UnsupportedOperationException() },
    open val getFlow: () -> Flow<T> = { throw UnsupportedOperationException() },
    open val getLiveData: () -> LiveData<T> = { throw UnsupportedOperationException() },
) {
    val value: T get() = getValue()
    val flow: Flow<T> get() = getFlow()
    val liveData: LiveData<T> get() = getLiveData()
}