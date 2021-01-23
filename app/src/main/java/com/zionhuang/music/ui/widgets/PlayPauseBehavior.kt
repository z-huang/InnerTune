package com.zionhuang.music.ui.widgets

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.zionhuang.music.R
import com.zionhuang.music.extensions.getAnimatedVectorDrawable

class PlayPauseBehavior(context: Context) {
    private val playAnimation = context.getAnimatedVectorDrawable(R.drawable.avd_play_to_pause)
    private val pauseAnimation = context.getAnimatedVectorDrawable(R.drawable.avd_pause_to_play)
    private val playDrawable = ContextCompat.getDrawable(context, R.drawable.ic_play_arrow_black_24dp)
    private val pauseDrawable = ContextCompat.getDrawable(context, R.drawable.ic_baseline_pause_48)
    private val callbacks = mutableMapOf<PlayPauseButton, AnimationCallback>()

    fun animatePlay(button: PlayPauseButton) {
        if (button.drawable != pauseDrawable) {
            with(playAnimation) {
                button.setImageDrawable(this)
                start()
                registerAnimationCallback(callbacks[button]
                        ?: AnimationCallback(button).also { callbacks[button] = it })
            }
        }
    }

    fun animationPause(button: PlayPauseButton) {
        if (button.drawable != playDrawable) {
            with(pauseAnimation) {
                button.setImageDrawable(this)
                start()
                registerAnimationCallback(callbacks[button]
                        ?: AnimationCallback(button).also { callbacks[button] = it })
            }
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