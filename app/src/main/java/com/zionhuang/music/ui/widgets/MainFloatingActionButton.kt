package com.zionhuang.music.ui.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.zionhuang.music.R

class MainFloatingActionButton : FloatingActionButton {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private val bottomSheetHeight = context.resources.getDimension(R.dimen.bottom_controls_sheet_peek_height)

    fun onPlayerShow() {
        animate().apply {
            translationY(-bottomSheetHeight)
            interpolator = FastOutSlowInInterpolator()
            duration = 200
        }
    }

    fun onPlayerHide() {
        animate().apply {
            translationY(0f)
            interpolator = FastOutSlowInInterpolator()
            duration = 200
        }
    }
}