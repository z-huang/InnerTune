package com.zionhuang.music.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

fun <T : ViewDataBinding> ViewGroup.inflateWithBinding(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): T =
    DataBindingUtil.inflate(LayoutInflater.from(context), layoutRes, this, attachToRoot) as T

fun View.getActivity(): Activity? = context.getActivity()

fun View.fadeIn(duration: Long) {
    isVisible = true
    alpha = 0f
    animate()
        .alpha(1f)
        .setDuration(duration)
        .setListener(null)
}

fun View.fadeOut(duration: Long) {
    isVisible = true
    alpha = 1f
    animate()
        .alpha(0f)
        .setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                isVisible = false
            }
        })
}