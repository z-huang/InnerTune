package com.zionhuang.music.ui.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.zionhuang.music.R
import com.zionhuang.music.extensions.getAnimatedVectorDrawable


class SortOrderImageView : AppCompatImageView {
    private var state: State = State.DOWN
    private val arrowUpwardDrawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow_upward)
    private val arrowDownwardDrawable = ContextCompat.getDrawable(context, R.drawable.ic_arrow_downward)
    private val arrowUpToDownAnimation = context.getAnimatedVectorDrawable(R.drawable.avd_arrow_up_to_down)
    private val arrowDownToUpAnimation = context.getAnimatedVectorDrawable(R.drawable.avd_arrow_down_to_up)
    private val animationCallback = object : Animatable2Compat.AnimationCallback() {
        override fun onAnimationEnd(drawable: Drawable?) {
            when (drawable) {
                arrowUpToDownAnimation -> setImageDrawable(arrowDownwardDrawable)
                arrowDownToUpAnimation -> setImageDrawable(arrowUpwardDrawable)
            }
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun animateToUp(animate: Boolean) {
        if (state != State.UP) {
            state = State.UP
            if (animate) {
                setAvd(arrowDownToUpAnimation)
            } else {
                setImageDrawable(arrowUpwardDrawable)
            }
        }
    }

    fun animateToDown(animate: Boolean) {
        if (state != State.DOWN) {
            state = State.DOWN
            if (animate) {
                setAvd(arrowUpToDownAnimation)
            } else {
                setImageDrawable(arrowDownwardDrawable)
            }
        }
    }

    private fun setAvd(avd: AnimatedVectorDrawableCompat) {
        with(avd) {
            setImageDrawable(this)
            start()
            registerAnimationCallback(animationCallback)
        }
    }

    enum class State {
        UP, DOWN
    }
}