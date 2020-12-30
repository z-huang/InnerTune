package com.zionhuang.music.ui.listeners

import androidx.annotation.IdRes

interface SortMenuListener {
    @IdRes
    fun sortType(): Int
    fun sortByCreateDate()
    fun sortByName()
    fun sortByArtist()
}