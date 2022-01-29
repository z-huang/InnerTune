package com.zionhuang.music.utils.livedata

open class SafeMutableLiveData<T : Any>(value: T) : SafeLiveData<T>(value) {
    public override fun postValue(value: T) = super.postValue(value)
    public override fun setValue(value: T) = super.setValue(value)
}