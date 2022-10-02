package com.zionhuang.music.ui.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View

class NavigationBarView : View {
    private val statusBarHeight: Int
        @SuppressLint("InternalInsetResource")
        get() = resources.getIdentifier("navigation_bar_height", "dimen", "android").takeIf { it != 0 }?.let {
            resources.getDimensionPixelSize(it)
        } ?: 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), statusBarHeight)
    }
}