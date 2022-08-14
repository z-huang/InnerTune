package com.zionhuang.music.extensions

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Color
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.use
import androidx.lifecycle.LifecycleOwner
import androidx.preference.PreferenceManager
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.zionhuang.music.utils.preference.Preference
import com.zionhuang.music.utils.preference.PreferenceLiveData
import com.zionhuang.music.utils.preference.serializablePreference

fun Context.getDensity(): Float = resources.displayMetrics.density

tailrec fun Context?.getActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    else -> (this as? ContextWrapper)?.baseContext?.getActivity()
}

tailrec fun Context?.getLifeCycleOwner(): LifecycleOwner? = when (this) {
    is LifecycleOwner -> this
    else -> (this as? ContextWrapper)?.baseContext?.getLifeCycleOwner()
}

@ColorInt
fun Context.resolveColor(@AttrRes attr: Int): Int = obtainStyledAttributes(intArrayOf(attr)).use {
    it.getColor(0, Color.MAGENTA)
}

fun Context.getAnimatedVectorDrawable(@DrawableRes id: Int): AnimatedVectorDrawableCompat =
    AnimatedVectorDrawableCompat.create(this, id)!!

val Context.sharedPreferences: SharedPreferences
    get() = PreferenceManager.getDefaultSharedPreferences(this)

fun <T : Any> Context.preference(@StringRes keyId: Int, defaultValue: T) = Preference(this, keyId, defaultValue)
inline fun <reified T : Any> Context.serializablePreference(keyId: Int, defaultValue: T): Preference<T> = serializablePreference(this, keyId, defaultValue)

fun <T : Any> Context.preferenceLiveData(@StringRes keyId: Int, defaultValue: T) = PreferenceLiveData(this, keyId, defaultValue)
