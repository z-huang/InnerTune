package com.zionhuang.music.playback

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.LruCache
import coil.imageLoader
import coil.request.Disposable
import coil.request.ImageRequest

class BitmapProvider(private val context: Context) {
    private val map = LruCache<String, Bitmap>(MAX_CACHE_SIZE)

    private var disposable: Disposable? = null

    fun load(url: String, callback: (Bitmap) -> Unit): Bitmap? {
        val cache = map.get(url)
        disposable?.dispose()
        if (cache == null) {
            disposable = context.imageLoader.enqueue(ImageRequest.Builder(context)
                .data(url)
                .target(onSuccess = { drawable ->
                    val bitmap = (drawable as BitmapDrawable).bitmap
                    map.put(url, bitmap)
                    callback(bitmap)
                })
                .build())
        }
        return cache
    }

    companion object {
        const val MAX_CACHE_SIZE = 10
    }
}