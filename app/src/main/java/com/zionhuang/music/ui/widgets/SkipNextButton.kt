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


class SkipNextButton : AppCompatImageView {
    private val skipNextDrawable = ContextCompat.getDrawable(context, R.drawable.ic_skip_next)
    private val skipNextAnimation = context.getAnimatedVectorDrawable(R.drawable.avd_skip_next)
    private val animationCallback = object : Animatable2Compat.AnimationCallback() {
        override fun onAnimationEnd(drawable: Drawable?) {
            setImageDrawable(skipNextDrawable)
        }
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
        setImageDrawable(skipNextDrawable)
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener {
            with(skipNextAnimation) {
                setImageDrawable(this)
                start()
                registerAnimationCallback(animationCallback)
            }
            l?.onClick(this)
        }
    }
}