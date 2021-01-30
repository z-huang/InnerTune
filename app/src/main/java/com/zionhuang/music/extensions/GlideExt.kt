package com.zionhuang.music.extensions

import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.zionhuang.music.utils.GlideRequest

fun GlideRequest<*>.circle() = apply(RequestOptions.bitmapTransform(CircleCrop()))

fun GlideRequest<*>.fullResolution() = override(Target.SIZE_ORIGINAL)

fun GlideRequest<*>.roundCorner(px: Int) = transform(MultiTransformation(CenterCrop(), RoundedCorners(px)))