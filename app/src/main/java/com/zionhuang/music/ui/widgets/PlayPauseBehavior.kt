package com.zionhuang.music.ui.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.zionhuang.music.R
import com.zionhuang.music.extensions.getAnimatedVectorDrawable

class PlayPauseBehavior(context: Context) {
    private val playAnimation = context.getAnimatedVectorDrawable(R.drawable.avd_play_to_pause)
    private val pauseAnimation = context.getAnimatedVectorDrawable(R.drawable.avd_pause_to_play)
    private val playDrawable = ContextCompat.getDrawable(context, R.drawable.ic_play)!!
    private val pauseDrawable = ContextCompat.getDrawable(context, R.drawable.ic_pause)!!
    private val callbacks = mutableMapOf<PlayPauseButton, AnimationCallback>()

    fun animatePlay(button: PlayPauseButton) {
        if (button.drawable.constantState != pauseDrawable.constantState) {
            button.setAvd(playAnimation)
        }
    }

    fun animationPause(button: PlayPauseButton) {
        if (button.drawable.constantState != playDrawable.constantState) {
            button.setAvd(pauseAnimation)
        }
    }

    private fun PlayPauseButton.setAvd(avd: AnimatedVectorDrawableCompat) {
        with(avd) {
            setImageDrawable(this)
            start()
            registerAnimationCallback(callbacks[this@setAvd]
                    ?: AnimationCallback(this@setAvd).also { callbacks[this@setAvd] = it })
        }
    }

    inner class AnimationCallback(private val button: PlayPauseButton) : Animatable2Compat.AnimationCallback() {
        override fun onAnimationEnd(drawable: Drawable?) {
            when (drawable) {
                playAnimation -> button.setImageDrawable(pauseDrawable)
                pauseAnimation -> button.setImageDrawable(playDrawable)
            }
        }
    }
}