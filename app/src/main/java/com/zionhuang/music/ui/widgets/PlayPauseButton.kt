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


class PlayPauseButton : AppCompatImageView {
    private val playAnimation = context.getAnimatedVectorDrawable(R.drawable.avd_play_to_pause)
    private val pauseAnimation = context.getAnimatedVectorDrawable(R.drawable.avd_pause_to_play)
    private val playDrawable = ContextCompat.getDrawable(context, R.drawable.ic_play)!!
    private val pauseDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pause)!!
    private val animationCallback = object : Animatable2Compat.AnimationCallback() {
        override fun onAnimationEnd(drawable: Drawable?) {
            when (drawable) {
                playAnimation -> setImageDrawable(pauseDrawable)
                pauseAnimation -> setImageDrawable(playDrawable)
            }
        }
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val tintResId = context.obtainStyledAttributes(attrs, R.styleable.PlayPauseButton).let {
            val res = it.getColor(R.styleable.PlayPauseButton_playPauseButtonTint, resources.getColor(R.color.colorInverted, context.theme))
            it.recycle()
            res
        }
        playAnimation.setTint(tintResId)
        pauseAnimation.setTint(tintResId)
        playDrawable.setTint(tintResId)
        pauseDrawable.setTint(tintResId)

        isClickable = true
        scaleType = ScaleType.CENTER_CROP
        setImageResource(R.drawable.ic_play)
    }

    fun animatePlay() {
        if (tag != STATE_PLAY) {
            setAvd(playAnimation)
            tag = STATE_PLAY
        }
    }

    fun animationPause() {
        if (tag != STATE_PAUSE) {
            setAvd(pauseAnimation)
            tag = STATE_PAUSE
        }
    }

    private fun setAvd(avd: AnimatedVectorDrawableCompat) {
        with(avd) {
            setImageDrawable(this)
            start()
            registerAnimationCallback(animationCallback)
        }
    }

    companion object {
        private const val STATE_PLAY = "PLAY"
        private const val STATE_PAUSE = "PAUSE"
    }
}