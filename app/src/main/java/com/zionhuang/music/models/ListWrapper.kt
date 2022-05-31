package com.zionhuang.music.models

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow


class ListWrapper<K : Any, V : Any>(
    val getList: suspend () -> List<V> = { throw UnsupportedOperationException() },
    val getPagingSource: () -> PagingSource<K, V> = { throw UnsupportedOperationException() },
    override val getFlow: () -> Flow<List<V>> = { throw UnsupportedOperationException() },
    override val getLiveData: () -> LiveData<List<V>> = { throw UnsupportedOperationException() },
) : DataWrapper<List<V>>(getValueAsync = getList) {
    val pagingSource: PagingSource<K, V> get() = getPagingSource()
}