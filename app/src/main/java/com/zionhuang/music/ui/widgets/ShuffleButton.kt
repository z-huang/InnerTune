package com.zionhuang.music.ui.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.zionhuang.music.R
import com.zionhuang.music.extensions.getAnimatedVectorDrawable

class ShuffleButton : AppCompatImageView {
    private var isActive = true

    private val shuffleDrawable = ContextCompat.getDrawable(context, R.drawable.ic_shuffle)
    private val showDrawable = context.getAnimatedVectorDrawable(R.drawable.avd_shuffle_show)
    private val hideDrawable = context.getAnimatedVectorDrawable(R.drawable.avd_shuffle_hide).apply {
        registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                setAlpha(if (isActive) 1f else 0.5f)
                setImageDrawable(showDrawable)
                showDrawable.start()
            }
        })
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        with(TypedValue()) {
            context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, this, true)
            setBackgroundResource(resourceId)
        }
        isClickable = true
        scaleType = ScaleType.CENTER_CROP
        setImageDrawable(shuffleDrawable)
    }

    fun enable() {
        if (!isActive) {
            isActive = true
            animateState()
        }
    }

    fun disable() {
        if (isActive) {
            isActive = false
            animateState()
        }
    }

    private fun animateState() {
        setImageDrawable(hideDrawable)
        hideDrawable.start()
    }
}