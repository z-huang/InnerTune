package com.zionhuang.music.utils

import androidx.lifecycle.LiveData

open class SafeLiveData<T : Any>(value:T) : LiveData<T>(value) {
    override fun getValue(): T = super.getValue()!!
}