package com.zionhuang.music.constants

import androidx.annotation.IntDef

@IntDef(ORDER_CREATE_DATE, ORDER_ARTIST, ORDER_NAME)
@Retention(AnnotationRetention.SOURCE)
annotation class SongSortType

const val ORDER_CREATE_DATE = 0
const val ORDER_NAME = 1
const val ORDER_ARTIST = 2
