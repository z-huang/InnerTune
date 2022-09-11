package com.zionhuang.music.models

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

class ListWrapper<Key : Any, Value : Any>(
    val getList: suspend () -> List<Value> = { throw UnsupportedOperationException() },
    override val getFlow: () -> Flow<List<Value>> = { throw UnsupportedOperationException() },
    override val getLiveData: () -> LiveData<List<Value>> = { throw UnsupportedOperationException() },
) : DataWrapper<List<Value>>(getValueAsync = getList)