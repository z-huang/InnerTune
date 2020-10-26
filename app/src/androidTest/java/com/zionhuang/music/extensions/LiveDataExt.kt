package com.zionhuang.music.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@Throws(InterruptedException::class)
fun <T> LiveData<T>.getValueBlocking(): T? {
    var value: T? = null
    val latch = CountDownLatch(1)
    val innerObserver = Observer<T> {
        value = it
        latch.countDown()
    }
    observeForever(innerObserver)
    latch.await(2, TimeUnit.SECONDS)
    return value
}