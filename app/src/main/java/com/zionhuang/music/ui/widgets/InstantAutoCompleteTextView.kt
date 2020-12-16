package com.zionhuang.music.ui.widgets

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import com.google.android.material.R
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class InstantAutoCompleteTextView : MaterialAutoCompleteTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet, R.attr.autoCompleteTextViewStyle)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr)

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused && filter != null) {
            performFiltering(text, 0)
        }
    }
}