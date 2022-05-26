package com.zionhuang.music.ui.viewholders.base

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.RecyclerView

open class LifecycleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), LifecycleOwner {
    @Suppress("LeakingThis")
    private val lifecycleRegistry = LifecycleRegistry(this)

    init {
        lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        itemView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View?) {
                onAttach()
            }

            override fun onViewDetachedFromWindow(v: View?) {
                onDetach()
            }
        })
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    open fun onAttach() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    open fun onDetach() {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }
}