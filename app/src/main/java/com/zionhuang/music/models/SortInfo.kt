package com.zionhuang.music.models

import com.zionhuang.music.constants.ORDER_CREATE_DATE
import com.zionhuang.music.constants.SongSortType

data class SortInfo(
    @SongSortType val order: Int = ORDER_CREATE_DATE,
    val descending: Boolean = true,
)
