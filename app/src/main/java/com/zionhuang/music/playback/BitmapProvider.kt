package com.zionhuang.music.playback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.LruCache
import coil.imageLoader
import coil.request.Disposable
import coil.request.ImageRequest

class BitmapProvider(private val context: Context) {
    var currentBitmap: Bitmap? = null
    private val map = LruCache<String, Bitmap>(MAX_CACHE_SIZE)
    private var disposable: Disposable? = null
    var onBitmapChanged: (Bitmap?) -> Unit = {}
        set(value) {
            field = value
            value(currentBitmap)
        }

    fun load(url: String, callback: (Bitmap) -> Unit): Bitmap? {
        val cache = map.get(url)
        disposable?.dispose()
        if (cache == null) {
            disposable = context.imageLoader.enqueue(ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .target(onSuccess = { drawable ->
                    val bitmap = (drawable as BitmapDrawable).bitmap
                    map.put(url, bitmap)
                    callback(bitmap)
                    currentBitmap = bitmap
                    onBitmapChanged(bitmap)
                })
                .build())
        } else {
            currentBitmap = cache
            onBitmapChanged(cache)
        }
        return cache
    }

    companion object {
        const val MAX_CACHE_SIZE = 15
    }
}