package com.zionhuang.music.extensions

import android.app.Activity
import android.view.WindowManager
import androidx.annotation.DimenRes
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun AppCompatActivity.replaceFragment(@IdRes id: Int, fragment: Fragment, tag: String? = null, addToBackStack: Boolean = false) {
    supportFragmentManager.beginTransaction().apply {
        replace(id, fragment, tag)
        if (addToBackStack) {
            addToBackStack(null)
        }
        commit()
    }
}

fun Activity.dip(@DimenRes id: Int): Int {
    return resources.getDimensionPixelSize(id)
}

fun AppCompatActivity.keepScreenOn(keepScreenOn: Boolean) {
    if (keepScreenOn) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}