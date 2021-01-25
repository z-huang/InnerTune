package com.zionhuang.music.extensions

import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.zionhuang.music.utils.GlideRequest

fun GlideRequest<*>.circle() = apply(RequestOptions.bitmapTransform(CircleCrop()))

fun GlideRequest<*>.fullResolution() = override(Target.SIZE_ORIGINAL)