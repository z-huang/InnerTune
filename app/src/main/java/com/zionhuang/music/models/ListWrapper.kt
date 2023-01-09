package com.zionhuang.music.models

import kotlinx.coroutines.flow.Flow

class ListWrapper<Value : Any>(
    val getList: suspend () -> List<Value> = { throw UnsupportedOperationException() },
    override val getFlow: () -> Flow<List<Value>> = { throw UnsupportedOperationException() },
) : DataWrapper<List<Value>>(getValueAsync = getList)