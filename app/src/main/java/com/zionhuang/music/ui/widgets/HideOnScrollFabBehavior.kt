package com.zionhuang.music.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.View.INVISIBLE
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HideOnScrollFabBehavior(context: Context, attrs: AttributeSet) : FloatingActionButton.Behavior(context, attrs) {

    // changes visibility from GONE to INVISIBLE when fab is hidden because
    // due to CoordinatorLayout.onStartNestedScroll() implementation
    // child view's (here, fab) onStartNestedScroll won't be called anymore
    // because it's visibility is GONE
    private val listener = object : FloatingActionButton.OnVisibilityChangedListener() {
        override fun onHidden(fab: FloatingActionButton) {
            fab.visibility = INVISIBLE
        }
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, directTargetChild: View, target: View, axes: Int, type: Int): Boolean =
            axes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)

    override fun onNestedScroll(coordinatorLayout: CoordinatorLayout, child: FloatingActionButton, target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
        if (dyConsumed > 0 && child.isVisible) {
            child.hide(listener)
        } else if (dyConsumed < 0 && !child.isVisible) {
            child.show()
        }
    }
}