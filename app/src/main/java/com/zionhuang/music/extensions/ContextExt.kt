package com.zionhuang.music.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.content.res.use
import com.zionhuang.music.utils.preference.Preference

fun Context.getDensity(): Float = resources.displayMetrics.density

tailrec fun Context?.getActivity(): Activity? = when (this) {
    is Activity -> this
    else -> (this as? ContextWrapper)?.baseContext?.getActivity()
}

@ColorInt
fun Context.themeColor(@AttrRes themeAttrId: Int): Int = obtainStyledAttributes(intArrayOf(themeAttrId)).use {
    it.getColor(0, Color.MAGENTA)
}

fun <T : Any> Context.preference(@StringRes keyId: Int, defaultValue: T) = Preference({ this }, keyId, defaultValue)