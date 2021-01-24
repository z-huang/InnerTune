package com.zionhuang.music.ui.listeners

import androidx.annotation.IdRes

interface SortMenuListener {
    @IdRes
    fun sortType(): Int
    fun sortDescending(): Boolean
    fun sortByCreateDate()
    fun sortByName()
    fun sortByArtist()
    fun toggleSortOrder()
}