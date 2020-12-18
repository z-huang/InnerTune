package com.zionhuang.music.extensions

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

fun <T : ViewDataBinding> ViewGroup.inflateWithBinding(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): T =
        DataBindingUtil.inflate(LayoutInflater.from(context), layoutRes, this, attachToRoot) as T

fun View.getActivity(): Activity? = context.getActivity()