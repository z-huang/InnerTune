package com.zionhuang.music.extensions

import android.os.Build
import android.view.WindowInsets
import androidx.annotation.RequiresApi
import androidx.core.graphics.Insets


val WindowInsets.systemBarInsetsCompat: Insets
    get() = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> getCompatInsets(WindowInsets.Type.systemBars())
        else -> getSystemWindowCompatInsets()
    }

@RequiresApi(Build.VERSION_CODES.R)
fun WindowInsets.getCompatInsets(typeMask: Int) = Insets.toCompatInsets(getInsets(typeMask))

@Suppress("DEPRECATION")
fun WindowInsets.getSystemWindowCompatInsets() = Insets.of(
    systemWindowInsetLeft,
    systemWindowInsetTop,
    systemWindowInsetRight,
    systemWindowInsetBottom
)

val WindowInsets.systemGestureInsetsCompat: Insets
    get() = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            Insets.max(
                getCompatInsets(WindowInsets.Type.systemGestures()),
                getCompatInsets(WindowInsets.Type.systemBars())
            )
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            @Suppress("DEPRECATION")
            Insets.max(getSystemGestureCompatInsets(), getSystemWindowCompatInsets())
        }
        else -> getSystemWindowCompatInsets()
    }

@Suppress("DEPRECATION")
@RequiresApi(Build.VERSION_CODES.Q)
fun WindowInsets.getSystemGestureCompatInsets() = Insets.toCompatInsets(systemGestureInsets)
