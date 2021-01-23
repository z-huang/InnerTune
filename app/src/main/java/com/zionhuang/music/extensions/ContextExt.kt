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
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.zionhuang.music.utils.preference.Preference
import java.io.File

fun Context.getDensity(): Float = resources.displayMetrics.density

tailrec fun Context?.getActivity(): Activity? = when (this) {
    is Activity -> this
    else -> (this as? ContextWrapper)?.baseContext?.getActivity()
}

@ColorInt
fun Context.themeColor(@AttrRes themeAttrId: Int): Int = obtainStyledAttributes(intArrayOf(themeAttrId)).use {
    it.getColor(0, Color.MAGENTA)
}

fun Context.getAnimatedVectorDrawable(@DrawableRes id: Int): AnimatedVectorDrawableCompat =
        AnimatedVectorDrawableCompat.create(this, id)!!

fun <T : Any> Context.preference(@StringRes keyId: Int, defaultValue: T) = Preference({ this }, keyId, defaultValue)

val Context.externalFilesDir: File get() = getExternalFilesDir(null)!!
val Context.artworkDir get() = File(externalFilesDir, "artwork")
val Context.audioDir get() = File(externalFilesDir, "audio")
val Context.channelDir get() = File(externalFilesDir, "channel")

fun Context.getAudioFile(songId: String) = File(audioDir, songId)
fun Context.getArtworkFile(songId: String) = File(artworkDir, songId)
fun Context.getChannelBannerFile(channelId: String) = File(channelDir, "${channelId}_banner")
fun Context.getChannelAvatarFile(channelId: String) = File(channelDir, "${channelId}_avatar")