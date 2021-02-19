package com.zionhuang.music.ui.widgets

import android.content.Context
import android.support.v4.media.session.PlaybackStateCompat.*
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatImageView
import com.zionhuang.music.R
import com.zionhuang.music.extensions.getAnimatedVectorDrawable

class RepeatButton : AppCompatImageView {
    @RepeatMode
    private var currentState = REPEAT_MODE_NONE

    private val showAnimation = context.getAnimatedVectorDrawable(R.drawable.avd_repeat_show)
    private val showOneAnimation = context.getAnimatedVectorDrawable(R.drawable.avd_repeat_show_one)
    private val hideAnimation = context.getAnimatedVectorDrawable(R.drawable.avd_repeat_hide)

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        with(TypedValue()) {
            context.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, this, true)
            setBackgroundResource(resourceId)
        }
        isClickable = true
        scaleType = ScaleType.CENTER_CROP
        setImageDrawable(hideAnimation)
        hideAnimation.start()
    }

    fun setState(@RepeatMode state: Int) {
        if (state != currentState) {
            when (state) {
                REPEAT_MODE_NONE, REPEAT_MODE_INVALID -> {
                    setImageDrawable(hideAnimation)
                    hideAnimation.start()
                }
                REPEAT_MODE_ALL, REPEAT_MODE_GROUP -> {
                    setImageDrawable(showAnimation)
                    showAnimation.start()
                }
                REPEAT_MODE_ONE -> {
                    setImageDrawable(showOneAnimation)
                    showOneAnimation.start()
                }
            }
            currentState = state
        }
    }
}