package com.zionhuang.music.extensions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import me.zhanghai.android.fastscroll.FastScroller
import me.zhanghai.android.fastscroll.FastScrollerBuilder

typealias RecyclerViewItemClickListener = (position: Int, view: View) -> Unit

fun RecyclerView.addOnClickListener(clickListener: RecyclerViewItemClickListener) {
    this.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewAttachedToWindow(view: View) {
            view.setOnClickListener {
                val position = this@addOnClickListener.getChildLayoutPosition(view)
                if (position >= 0) {
                    clickListener.invoke(position, view)
                }
            }
        }

        override fun onChildViewDetachedFromWindow(view: View) {
            view.setOnClickListener(null)
        }
    })
}

fun RecyclerView.addOnLongClickListener(longClickListener: RecyclerViewItemClickListener) {
    this.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
        override fun onChildViewAttachedToWindow(view: View) {
            view.setOnLongClickListener {
                val position = this@addOnLongClickListener.getChildLayoutPosition(view)
                if (position >= 0) {
                    longClickListener.invoke(position, view)

                }
                return@setOnLongClickListener true
            }
        }

        override fun onChildViewDetachedFromWindow(view: View) {
            view.setOnLongClickListener(null)
        }
    })
}

fun RecyclerView.addFastScroller(applier: (FastScrollerBuilder.() -> Unit)): FastScroller =
    FastScrollerBuilder(this).apply(applier).build()