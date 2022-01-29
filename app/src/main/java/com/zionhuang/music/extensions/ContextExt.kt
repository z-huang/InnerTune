package com.zionhuang.music.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.use
import androidx.lifecycle.LifecycleOwner
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.zionhuang.music.utils.preference.Preference
import com.zionhuang.music.utils.preference.PreferenceLiveData
import java.io.File

fun Context.getDensity(): Float = resources.displayMetrics.density

tailrec fun Context?.getActivity(): Activity? = when (this) {
    is Activity -> this
    else -> (this as? ContextWrapper)?.baseContext?.getActivity()
}

tailrec fun Context?.getLifeCycleOwner(): LifecycleOwner? = when (this) {
    is LifecycleOwner -> this
    else -> (this as? ContextWrapper)?.baseContext?.getLifeCycleOwner()
}

@ColorInt
fun Context.themeColor(@AttrRes themeAttrId: Int): Int = obtainStyledAttributes(intArrayOf(themeAttrId)).use {
    it.getColor(0, Color.MAGENTA)
}

fun Context.getAnimatedVectorDrawable(@DrawableRes id: Int): AnimatedVectorDrawableCompat =
    AnimatedVectorDrawableCompat.create(this, id)!!

fun <T : Any> Context.preference(@StringRes keyId: Int, defaultValue: T) = Preference({ this }, keyId, defaultValue)
fun <T : Any> Context.preferenceLiveData(@StringRes keyId: Int, defaultValue: T) = PreferenceLiveData(this, keyId, defaultValue)

val Context.externalFilesDir: File get() = getExternalFilesDir(null)!!
val Context.artworkDir get() = externalFilesDir / "artwork"
val Context.audioDir get() = externalFilesDir / "audio"
val Context.channelDir get() = externalFilesDir / "channel"
val Context.recycleDir get() = externalFilesDir / "recycle"

fun Context.getAudioFile(songId: String) = audioDir / songId
fun Context.getRecycledAudioFile(songId: String) = recycleDir / "audio" / songId
fun Context.getArtworkFile(songId: String) = artworkDir / songId
fun Context.getRecycledArtworkFile(songId: String) = recycleDir / "artwork" / songId
fun Context.getChannelBannerFile(channelId: String) = channelDir / "${channelId}_banner"
fun Context.getChannelAvatarFile(channelId: String) = channelDir / "${channelId}_avatar"