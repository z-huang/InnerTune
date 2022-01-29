package com.zionhuang.music.repos

import androidx.paging.PagingSource

data class ListWrapper<K : Any, V : Any>(
    val list: (suspend () -> List<V>)? = throw UnsupportedOperationException(),
    val pagingSource: (() -> PagingSource<K, V>)? = throw UnsupportedOperationException(),
)