package com.zionhuang.music.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatImageView
import com.zionhuang.music.R


class PlayPauseButton : AppCompatImageView {
    private lateinit var behavior: PlayPauseBehavior

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        with(TypedValue()) {
            context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, this, true)
            setBackgroundResource(resourceId)
        }
        isClickable = true
        scaleType = ScaleType.CENTER_CROP
        setImageResource(R.drawable.ic_play)
    }

    fun setBehavior(behavior: PlayPauseBehavior) {
        this.behavior = behavior
    }

    fun animatePlay() {
        behavior.animatePlay(this)
    }

    fun animationPause() {
        behavior.animationPause(this)
    }
}