package com.zionhuang.music.extensions

import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.zionhuang.music.utils.GlideRequest

fun GlideRequest<*>.circle() = apply(RequestOptions.bitmapTransform(CircleCrop()))