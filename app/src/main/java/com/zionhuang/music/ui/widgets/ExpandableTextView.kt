package com.zionhuang.music.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView

class ExpandableTextView : AppCompatTextView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var selectableBackground: Int = 0
    private var isExpanded = false

    init {
        with(TypedValue()) {
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, this, true)
            selectableBackground = resourceId
            setBackgroundResource(resourceId)
        }
        maxLines = MAX_LINE_COLLAPSED
        setOnClickListener {
            toggleExpand()
        }
        isSaveEnabled = true
    }

    private fun toggleExpand() {
        maxLines = if (isExpanded) MAX_LINE_COLLAPSED else Int.MAX_VALUE
        isExpanded = !isExpanded
    }

    companion object {
        const val MAX_LINE_COLLAPSED = 3
    }
}