package com.zionhuang.music.utils.livedata

import androidx.lifecycle.LiveData

open class SafeLiveData<T : Any>(value: T) : LiveData<T>(value) {
    override fun getValue(): T = super.getValue()!!
}